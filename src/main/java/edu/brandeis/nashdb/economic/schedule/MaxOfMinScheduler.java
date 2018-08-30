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
 
package edu.brandeis.nashdb.economic.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.cloud.Subquery;
import edu.brandeis.nashdb.economic.cloud.VM;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public class MaxOfMinScheduler implements Scheduler {

	
	private Collection<Fragment> fragments;
	
	public MaxOfMinScheduler(Collection<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	@Override
	public void selectVM(Collection<VM> vms, Query q) {
		List<VM> orderedVMs = new ArrayList<>(vms);
		List<Fragment> neededFrags = new ArrayList<Fragment>(q.getRelevantFragments(fragments));
		
		int[] queueTimes = new int[vms.size()];
		boolean[] usingVM = new boolean[vms.size()];
		
		// compute the queue time + "open new VM" time
		// for each VM
		for (int i = 0; i < orderedVMs.size(); i++) {
			queueTimes[i] = orderedVMs.get(i).getRunningTime() + VM.NETWORK_OVERHEAD;
		}
		
		boolean isFirst = true;
		
		while (!neededFrags.isEmpty()) {
			// next, find the fragment with the maximum minimum queue time
			List<Integer> minWaitTimes = new ArrayList<>(neededFrags.size());
			Map<Fragment, Integer> bestVMForFrag = new HashMap<>();
			
			int maxMinWaitIdx = 0;
			
			for (Fragment f : neededFrags) {
				// find the minimum wait time for this fragment.
				int minWait = Integer.MAX_VALUE;
				for (int i = 0; i < queueTimes.length; i++) {
					if (orderedVMs.get(i).hasFragment(f) && queueTimes[i] < minWait) {
						minWait = queueTimes[i];
						bestVMForFrag.put(f, i);
					}
				}
				
				minWaitTimes.add(minWait);
				if (minWait > minWaitTimes.get(maxMinWaitIdx))
					maxMinWaitIdx = minWaitTimes.size() - 1;
			}

			// find the argmax of the minWaitTimes
//			int maxMinWaitIdx = IntStream.range(0, minWaitTimes.size())
//					.boxed()
//					.max((a, b) -> minWaitTimes.get(a) - minWaitTimes.get(b))
//					.get();

			Fragment maxMinWait = neededFrags.get(maxMinWaitIdx);

			// schedule this fragment on that VM
			int bestVMIndex = bestVMForFrag.get(maxMinWait);
			VM bestVM = orderedVMs.get(bestVMIndex);
			
			Subquery sq = new Subquery(maxMinWait, q, bestVM);
			if (isFirst) {
				sq.markBiggest();
				isFirst = false;
			}
			bestVM.addQueryToQueue(sq);
			if (!usingVM[bestVMIndex]) {
				usingVM[bestVMIndex] = true;
				queueTimes[bestVMIndex] -= VM.NETWORK_OVERHEAD;
			}
			queueTimes[bestVMIndex] += sq.getSize();

			neededFrags.remove(maxMinWaitIdx);
		}
	}

}
