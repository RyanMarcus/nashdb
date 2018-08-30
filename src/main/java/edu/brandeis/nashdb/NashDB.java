// < begin copyright > 
// Copyright Ryan Marcus 2018
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
// 
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
// 
// < end copyright > 
 
package edu.brandeis.nashdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import edu.brandeis.nashdb.economic.cloud.NashEquilbFinder;
import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.cloud.VM;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.density.DensityEstimator.DensityIterator;
import edu.brandeis.nashdb.economic.fragments.AlternatingStrategy;
import edu.brandeis.nashdb.economic.fragments.OptimalStrategy;
import edu.brandeis.nashdb.economic.fragments.Strategy;
import edu.brandeis.nashdb.economic.fragments.TransitionCostCalculator;

/**
 * This is the primary entry point for our public implementation
 * of the NashDB algorithms. The algorithms, and overall system,
 * are described in detail in this paper and talk:
 * 
 *     <a href="http://rm.cab/sigmod18">http://rm.cab/sigmod18</a>
 *     <a href="http://rm.cab/nashdb">http://rm.cab/nashdb</a>
 *     
 * This code supports the following functionalities:
 * <ul>
 * <li>Building a tuple value estimator and traversing it</li>
 * <li>Perform value-based fragmentation, using either a greedy (O(n)) or optimal (O(n^3)) algorithm</li>
 * <li>Compute clusters in Nash equilibriums for a given set of fragments</li>
 * <li>Compute optimal transition stratieges between arbitrary cluster configurations</li>
 * </ul>
 * 
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class NashDB {

	private final int numTuples;
	private final DensityEstimator de;
	
	private final int vmSize;
	private final int vmCost;
	private final int timeWindow;
	
	private boolean splitLargeFragments = true;
	
	/**
	 * Construct a new instance of the NashDB API for the given number of tuples
	 * @param numTuples the number of tuples to track (0 - n)
	 * @return a NashDB instance
	 */
	public static NashDB freshNashDBInstance(int numTuples) {
		DensityEstimator de = new DensityEstimator(500);
		return new NashDB(numTuples, de, 1, 200, 7000);
	}
	
	NashDB(int numTuples, DensityEstimator de, int vmCost, int vmSize, int timeWindow) {
		this.numTuples = numTuples;
		this.de = de;
		
		this.vmSize = vmSize;
		this.vmCost = vmCost;
		this.timeWindow = timeWindow;
	}
	
	/**
	 * Returns the intervals and values from the internal tuple value estimator
	 * 
	 * @return an ordered list of (start, stop, value) triplets
	 */
	public List<RangeAndValue> getEstimatedTupleValues() {
		DensityIterator di = de.iterator();
		
		List<RangeAndValue> toReturn = new ArrayList<>();
		
		while (di.hasNext()) {
			toReturn.add(new RangeAndValue(
					di.getCurrentPoint(), 
					Math.min(di.getCurrentEnd(), numTuples),
					di.getCount()));
			
			di.next();
		}
		
		
		return Collections.unmodifiableList(toReturn);
	}
	
	/**
	 * Adds a range scan to the tuple value estimator by inserting its start
	 * and end points into the tree. This is a O(log n) operation, where n is the size
	 * of the sliding window (default 500).
	 * 
	 * @param start the first tuple read by the scan
	 * @param stop the end of the range scan (exclusive)
	 * @param value the value of the scan 
	 */
	public void noteQuery(int start, int stop, int value) {
		de.addQuery(new Query(start, stop, value));
	}
	
	/**
	 * Performs value-based fragmentation using the given fragmentation strategy. Results in
	 * at least numFragments fragments, but could result in more depending on the VM size.
	 * @param fs the fragmentation strategy to use (e.g. optimal or greedy)
	 * @param numFragments the number of fragments to create
	 * @return the fragments
	 */
	public FragmentHolder doFragmentation(FragmentationStrategy fs, int numFragments) {
		Collection<edu.brandeis.nashdb.economic.fragments.Fragment> fragments = null;
		switch (fs) {
		case OPTIMAL:
			Strategy s = new OptimalStrategy(numFragments, numTuples);
			fragments = s.executeRound(de, null);
			break;
		case GREEDY:
			fragments = new ArrayList<>();
			fragments.add(new edu.brandeis.nashdb.economic.fragments.Fragment(0, numTuples));
			Strategy alt = new AlternatingStrategy(numFragments);

			for (int i = 0; i < 100; i++) {
				fragments = alt.executeRound(de, fragments);
			}
			break;
		}
		
		if (splitLargeFragments)
			fragments = Strategy.splitLarge(vmSize, fragments);
		
		Collection<Fragment> localFrags = new ArrayList<>(
				fragments.stream()
				.map(f -> new Fragment(f.getStart(), f.getStop()))
				.collect(Collectors.toList()));
		
		return new FragmentHolderWrapper(localFrags);
	}
	
	/**
	 * Computes a cluster configuration in Nash equilibrium from the passed fragments.
	 * 
	 * Each member of the returned collection itself represents a collection of fragments,
	 * corresponding to a single VM.
	 * 
	 * @param fragments the fragments to replicate and distribute
	 * @return the cluster configuration
	 */
	public Collection<Collection<Fragment>> doFragmentAllocation(Collection<Fragment> fragments) {
		
		Collection<edu.brandeis.nashdb.economic.fragments.Fragment> internalFragments = fragments.stream()
				.map(f -> new edu.brandeis.nashdb.economic.fragments.Fragment(f.getStart(), f.getStop()))
				.collect(Collectors.toList());

		// get the set of VMs
		Collection<VM> vms = NashEquilbFinder.getVMs(de, internalFragments, vmSize, vmCost, timeWindow);
		
		Collection<Collection<Fragment>> toReturn = new ArrayList<>(vms.size());
		
		for (VM vm : vms) {
			toReturn.add(vm.getFragments().stream()
					.map(f -> new Fragment(f.getStart(), f.getStop()))
					.collect(Collectors.toSet()));
		}
		
		return toReturn;
	}
	
	/**
	 * Produces useful information in JSON form about the current tuple value estimator and fragmentation.
	 * @param fs the fragmentation scheme to use
	 * @param numFragments the number of fragments to create
	 * @return
	 */
	public String toJSON(FragmentationStrategy fs, int numFragments) {		
		JsonArray graphData = new JsonArray();
		for (RangeAndValue rav : getEstimatedTupleValues())
			graphData.add(rav.toJSON());
		
		List<Fragment> fragments = new ArrayList<>(doFragmentation(fs, numFragments).getFragments());
		Map<Integer, Integer> startingPointToFragmentIndex = new HashMap<>();
		JsonArray jsonFragments = new JsonArray();
		
		for (int fragmentIndex = 0; fragmentIndex < fragments.size(); fragmentIndex++) {
			Fragment f = fragments.get(fragmentIndex);
			jsonFragments.add(f.toJSON());

			startingPointToFragmentIndex.put(f.getStart(), fragmentIndex);
		}
		
		JsonArray jsonVMs = new JsonArray();
		Collection<Collection<Fragment>> vms = doFragmentAllocation(fragments);
		
		for (Collection<Fragment> vm : vms) {
			JsonArray vmFragments = new JsonArray();
			for (Fragment f : vm) {
				vmFragments.add(startingPointToFragmentIndex.get(f.getStart()));
			}
			
			JsonObject jsonVM = new JsonObject();
			jsonVM.add("fragments", vmFragments);
			jsonVMs.add(jsonVM);
		}
		
		JsonObject toReturn = new JsonObject();
		toReturn.add("graphData", graphData);
		toReturn.add("fragments", jsonFragments);
		toReturn.add("vms", jsonVMs);
		return toReturn.toString();
	}
	
	/**
	 * Computes an optimal cluster transition policy between the "current" and "target"
	 * cluster configurations. The returned TransitionAction instances will contain references
	 * (not copies) to the passed-in FragmentHolder instances.
	 * @param current the current cluster configuration
	 * @param target the new cluster configuration
	 * @return a strategy for optimal transitioning
	 */
	public <T extends FragmentHolder> Collection<TransitionAction<T>> computeTransition(List<T> current, List<T> target) {
		// transform the lists of fragment holders into a list of collections of fragments.
		List<Collection<Fragment>> before = current.stream().map(fh -> fh.getFragments()).collect(Collectors.toList());
		List<Collection<Fragment>> after = target.stream().map(fh -> fh.getFragments()).collect(Collectors.toList());

		List<VM> beforeF = before.stream()
				.map(frags -> new VM(vmSize, frags))
				.collect(Collectors.toList());
		List<VM> afterF = after.stream()
				.map(frags -> new VM(vmSize, frags))
				.collect(Collectors.toList());
		
		TransitionCostCalculator tcc = new TransitionCostCalculator(beforeF, afterF);
		Map<Long, Long> strategy = tcc.getTransitionStrategy();
		
		List<TransitionAction<T>> toReturn = new ArrayList<>(strategy.size());
		for (Entry<Long, Long> e : strategy.entrySet()) {
			int beforeIdx = e.getKey().intValue();
			int afterIdx = e.getValue().intValue();
				
			if (afterIdx < 0) {
				// it is a delete
				toReturn.add(TransitionAction.deleteAction(current.get(beforeIdx)));
			} else if (beforeIdx < 0) {
				// it is a provision
				toReturn.add(TransitionAction.provisionAction(target.get(afterIdx)));
			} else {		
				// it is a transition
				toReturn.add(TransitionAction.transitionAction(current.get(beforeIdx), target.get(afterIdx)));
			}
		}
		
		return toReturn;
		
	}
	
	private class FragmentHolderWrapper implements FragmentHolder {
		private final Collection<Fragment> fragments;
		
		private FragmentHolderWrapper(Collection<Fragment> fragments) {
			this.fragments = fragments;
		}

		@Override
		public Collection<Fragment> getFragments() {
			return fragments;
		}
		
	}
}
