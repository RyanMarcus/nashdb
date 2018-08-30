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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public class VM {
	private final int MAX_SIZE;
	public static final int COST = 1;
	public static final int STARTUP_COST = 3000;
	public static final int NETWORK_OVERHEAD = 50;
	
	private Deque<Subquery> queue;
	private Set<Fragment> fragments;
	private Set<Integer> coveredTuples;
	
	private boolean haveUnrecordedFragment;
	private List<Integer> latency = null;
	private int totalFragmentDensity;
	
	
	public VM(int maxSize) {
		this.MAX_SIZE = maxSize;
		fragments = new HashSet<>();
		queue = new LinkedList<>();
		coveredTuples = new HashSet<>();
	}
	
	public VM(int maxSize, Collection<edu.brandeis.nashdb.Fragment> frags) {
		this(maxSize);
		
		frags.forEach(f -> {
			this.addFragment(new Fragment(f));
		});
		fragments = Collections.unmodifiableSet(fragments);
	}
	
	public int getMaxSize() {
		return MAX_SIZE;
	}
	
	public boolean canFitFragment(Fragment f) {
		if (f.getSize() > MAX_SIZE)
			return false;
		
		if (fragments.isEmpty())
			return true;
		
		if (fragments.contains(f))
			return false;
		
		int currSum = fragments.stream()
				.mapToInt(fg -> fg.getSize())
				.sum();
		
		return currSum + f.getSize() <= MAX_SIZE;
	}
	
	public int getCapacity() {
		return MAX_SIZE;
	}
	
	
	
	public void addFragment(Fragment f) {
		if (!canFitFragment(f))
			throw new FragmentDoesNotFitException("Trying to add a fragment that won't fit");

		haveUnrecordedFragment = true;
		
		for (int i = f.getStart(); i < f.getStop(); i++) {
			coveredTuples.add(i);
		}
		
		fragments.add(f);
	}
	
	public void addFragment(Fragment f, DensityEstimator de) {
		if (!canFitFragment(f))
			throw new FragmentDoesNotFitException("Trying to add a fragment that won't fit");
	
		totalFragmentDensity += de.getDensityInFragment(f)
				.stream().mapToInt(i -> i).sum();
		
		for (int i = f.getStart(); i < f.getStop(); i++) {
			coveredTuples.add(i);
		}
		
		fragments.add(f);
	}
	
	public int getTotalFragmentDensity(DensityEstimator de) {
		if (haveUnrecordedFragment) {
			totalFragmentDensity = fragments.stream()
					.flatMap(f -> de.getDensityInFragment(f).stream())
					.mapToInt(i -> i).sum();
			haveUnrecordedFragment = false;
		}
		return totalFragmentDensity;
	}
	
	
	public void addQueryToQueue(Subquery q) {
		queue.add(q);
		latency = null;
		
		q.setFinishedAt(getRunningTime());
	}
	
	public List<Integer> getLatency() {
		if (latency != null)
			return latency;
		
		List<Integer> toR = new LinkedList<>();
		int currentStartTime = 0;
		
		Set<Query> penaltyPaid = new HashSet<>();
		for (Subquery q : queue) {
			int time = q.getSize();
			
			if ((!q.isBiggest()) && 
					q.getAssignedVM() != q.getQuery().getBiggestSubquery().getAssignedVM() &&
					!penaltyPaid.contains(q.getQuery())) {
				// pay a network penalty
				time += NETWORK_OVERHEAD;
				penaltyPaid.add(q.getQuery());
			}
			
			time += currentStartTime;
			currentStartTime = time;
			
			toR.add(time);
		}
		
		latency = toR;
		return toR;
	}
	
	public int getRunningTime() {
		List<Integer> lat = getLatency();
		if (lat.size() == 0)
			return 0;
		
		return lat.get(lat.size()-1);
	}
	

	
	public boolean isTupleCovered(int tuple) {
		return coveredTuples.contains(tuple);
	}
	
	public Set<Integer> getTuplesCovered(Set<Integer> tuples) {
		Set<Integer> toR = new HashSet<>(tuples);
		toR.retainAll(coveredTuples);
		return toR;
	}
	
	@Override
	public String toString() {
		return "[VM: l(" + getRunningTime() + ") " 
				+ "f(" + getFreeSpace() + ") "
				+ fragments.stream().sorted().map(f -> f.toString()).collect(Collectors.joining(" "))
				+ "]";
	}

	public void consolidateFragments() {
		List<Fragment> newFrags = new ArrayList<>();
		Deque<Fragment> adjFrags = new LinkedList<>();
		
		List<Fragment> curr = new ArrayList<>(fragments);
		Collections.sort(curr);
		
		for (Fragment f : curr) {
			if (adjFrags.isEmpty() || adjFrags.getLast().getStop() == f.getStart())
				adjFrags.add(f);
			else {
				newFrags.add(new Fragment(
						adjFrags.getFirst().getStart(), 
						adjFrags.getLast().getStop()));
				adjFrags.clear();
				adjFrags.add(f);
			}
		}
		
		newFrags.addAll(adjFrags);
		
		
		fragments = new TreeSet<>();
		
		for (Fragment f : newFrags) {
			fragments.add(f);
		}
	}
	
	public Collection<Fragment> getFragments() {
		return fragments;
	}
	
	Collection<Subquery> getSubqueries() {
		return queue;
	}

	public int getFreeSpace() {
		return this.getCapacity() - this.getUsedSpace();
	}

	public int getUsedSpace() {
		return fragments.stream().mapToInt(f -> f.getSize()).sum();
	}

	public boolean hasFragment(Fragment f) {
		return fragments.contains(f);
	}
		
	
}
