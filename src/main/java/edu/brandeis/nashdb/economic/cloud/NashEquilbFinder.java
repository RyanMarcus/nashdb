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
 
package edu.brandeis.nashdb.economic.cloud;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public class NashEquilbFinder {
	public static int minReplicas = 1;
	
	public static Map<Fragment, Integer> findEquib(DensityEstimator de, Collection<Fragment> f, int vmSize, int vmCost, double timeWindow) {
		Map<Fragment, Integer> toR = new HashMap<>();
		
		
		for (Fragment currFrag : f) {
			double costOfFragment = ((double)currFrag.getSize() / (double)vmSize) * (double)vmCost * timeWindow;
			double profitFromFragment = de.getDensityInFragment(currFrag).stream()
					.mapToDouble(i -> i).sum();
			double numReplicas = profitFromFragment / costOfFragment;
			
			int reps = (int) Math.max(minReplicas, numReplicas);
			
			currFrag.setExpectedProfit(profitFromFragment / (double)reps);
			
//			System.out.println("Cost: " + costOfFragment 
//					+ " profit: " + profitFromFragment
//					+ " numReps: " + numReplicas);
			
			toR.put(currFrag, reps);
		}
		
		
		return toR;
	}
	
	public static Set<VM> pack(int vmSize, Map<Fragment, Integer> frags) {
		// use the FFD class constrained algorithm
		
		List<VM> toR = new LinkedList<>();
		
		
		// TODO: sort by number of replicas, not replica size, first.
		
		// put our map in order of number of replicas
		List<Fragment> sortedFrags = frags.entrySet().stream()
				.sorted((a, b) -> a.getValue() - b.getValue())
				.map(e -> e.getKey())
				.collect(Collectors.toList());
		
		for (Fragment currFrag : sortedFrags) {
			int numReplicas = frags.get(currFrag);
			
			while (numReplicas --> 0) {
				boolean packed = false;
				for (VM toTry : toR) {
					if (toTry.canFitFragment(currFrag)) {
						toTry.addFragment(currFrag);
						packed = true;
						break;
					}
				}
				
				if (!packed) {
					// add a new VM
					VM vm = new VM(vmSize);
					vm.addFragment(currFrag);
					toR.add(vm);
				}
			}
		}
		
		// now we have packed everything required, allocate our excess resources
		// to the most popular fragments possible TODO re-enable for non-demo
//		for (VM vm : toR) {
//			for (Fragment extra : sortedFrags) {
//				if (vm.canFitFragment(extra)) {
//					vm.addFragment(extra);
//				}
//			}
//		}
		
		return new HashSet<>(toR);
	}
	
	public static Set<VM> getVMs(DensityEstimator de, Collection<Fragment> frags, int vmSize, int vmCost, int timeWindow) {
		Map<Fragment, Integer> freqs = findEquib(de, frags, vmSize, vmCost, timeWindow);
		return pack(vmSize, freqs);
	}
	

}
