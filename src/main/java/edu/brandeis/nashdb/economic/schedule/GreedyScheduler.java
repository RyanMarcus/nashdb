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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.cloud.Subquery;
import edu.brandeis.nashdb.economic.cloud.VM;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public class GreedyScheduler implements Scheduler {
	
	private final Collection<Fragment> fragments;
	private final Map<Fragment, Set<VM>> fragsToVM;
	
	public GreedyScheduler(Collection<Fragment> frags, Collection<VM> vms) {
		fragments = new ArrayList<>(frags);
		fragsToVM = new HashMap<>();
		
		
		for (VM vm : vms) {
			for (Fragment f : vm.getFragments()) {
				fragsToVM.putIfAbsent(f, new HashSet<>());
				fragsToVM.get(f).add(vm);
			}
		}
		
	}
	
	/**
	 * Selects the VM that will process the query with the lowest estimated latency
	 * for the given query.
	 */
	@Override
	public void selectVM(Collection<VM> vms, Query q) {
		// for each required fragment, find the VM with the lowest queue time
		// that has that fragment.
		List<Fragment> relevant = new ArrayList<>(q.getRelevantFragments(fragments));
		Collections.sort(relevant, (a, b) -> b.getSize() - a.getSize());
		
		boolean isFirst = true;
		for (Fragment needed : relevant) {
			// select the VM with the lowest current wait time
			VM best = fragsToVM.get(needed)
					.stream()
					.min((a, b) -> a.getRunningTime() - b.getRunningTime())
					.get();
			
			Subquery sq = new Subquery(needed, q, best);
			
			if (isFirst) {
				sq.markBiggest();
				isFirst = false;
			}
			
			best.addQueryToQueue(sq);

		}
	}
	
}
