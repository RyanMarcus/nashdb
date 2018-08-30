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
 
package edu.brandeis.nashdb.economic.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.density.FragmentDensityIterator;
import edu.brandeis.nashdb.economic.experiments.CalcUtils;
import edu.brandeis.nashdb.economic.fragments.FragmentSplitter.SplitData;

public class FragmentJoiner {
	
	private Map<Integer, Integer> bestSplitPoint;
	private Map<Integer, Double> varianceReduction;
	
	public FragmentJoiner() {

	}
	
	public List<Fragment> joinFragments(DensityEstimator de, List<Fragment> fragments) {
		bestSplitPoint = new HashMap<>();
		varianceReduction = new HashMap<>();
		
		Iterator<List<Integer>> fdi = new FragmentDensityIterator(fragments, de);
		Iterator<Fragment> it = fragments.iterator();
		
		// iterate over a sliding window of three fragments
		Deque<Fragment> fq = new LinkedList<>();
		Deque<List<Integer>> dq = new LinkedList<>();
		
		int count = 0;
		while (it.hasNext() && fdi.hasNext()) {
			fq.add(it.next());
			dq.add(fdi.next());
			
			if (fq.size() < 3 || dq.size() < 3)
				continue;
			
			while (fq.size() > 3)
				fq.removeFirst();
			while (dq.size() > 3)
				dq.removeFirst();
			
			evaluateTrigramForSplit(count, fq, dq);
			count++;
		}
		
		Entry<Integer, Double> pt = varianceReduction.entrySet()
				.stream()
				.filter(e -> e.getValue() >= 0.0)
				.max((a, b) -> (int) Math.signum(a.getValue() - b.getValue()))
				.orElse(null);
		
		if (pt == null) {
			// didn't get a reduction anywhere. don't join.
			return fragments;
		}
		
		int bestKey = pt.getKey();
		// the key is the first of the three fragments we are combining.
		List<Fragment> toR = new LinkedList<>(fragments);
		int start = toR.remove(bestKey).getStart();
		int mid = start + bestSplitPoint.get(bestKey) + 1;
		toR.remove(bestKey);
		int end = toR.remove(bestKey).getStop();
		
		
		FragmentSplitter.log.info("When considering " + fragments + " the best join is (" + start + ", " + mid + ", " + end + ")");
		//System.out.println(start + ", " + mid + ", " + end);
		
		toR.add(new Fragment(start, mid));
		toR.add(new Fragment(mid, end));
		
		Collections.sort(toR);
		
		//System.out.println("Reduced variance by " + varianceReduction.get(bestKey));
		
		return toR;
	}
	
	private void evaluateTrigramForSplit(int pt, Deque<Fragment> frags, Deque<List<Integer>> densities) {
		// create a fake super fragment to represent the combined three fragments
		double varianceBefore = 0.0;
		SumOfSquares v = new SumOfSquares();
		
		Fragment superFragment = new Fragment(frags.getFirst().getStart(), frags.getLast().getStop());
		List<Integer> allDensity = new ArrayList<>();
		for (List<Integer> density : densities) {
			varianceBefore += v.evaluate(density.stream().mapToDouble(i -> i).toArray());
			allDensity.addAll(density);
		}
		
		// find the best split point for the super fragment
		SplitData sd = FragmentSplitter.findSplitPoint(superFragment, allDensity);
				
		FragmentSplitter.log.info("Join at " + pt + " gives " + sd.varianceAfterSplit + " (before: " + varianceBefore + ")");
		
		bestSplitPoint.put(pt, sd.splitPoint);
		varianceReduction.put(pt, varianceBefore - sd.varianceAfterSplit);
	}
	
	public static void main(String[] args) {
		Fragment f1 = new Fragment(0, 10);
		Fragment f2 = new Fragment(10, 20);
		Fragment f3 = new Fragment(20, 30);
		Fragment f4 = new Fragment(30, 40);
		
		List<Fragment> fragments = new ArrayList<>();
		fragments.add(f1);
		fragments.add(f2);
		fragments.add(f3);
		fragments.add(f4);
		
		DensityEstimator de = new DensityEstimator();
		de.addQuery(new Query(0, 20));
		de.addQuery(new Query(20, 30));
		de.addQuery(new Query(19, 34));
		
		System.out.println(de.getFlatDensity(40));
		
		FragmentJoiner fj = new FragmentJoiner();
		System.out.println("Variance now: " + CalcUtils.computeVarianceSum(de, fragments));
		System.out.println(fragments);
		
		fragments = fj.joinFragments(de, fragments);
		System.out.println("Variance now: " + CalcUtils.computeVarianceSum(de, fragments));
		System.out.println(fragments);
		
	}
}
