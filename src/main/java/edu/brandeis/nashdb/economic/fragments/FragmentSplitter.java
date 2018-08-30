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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.density.FragmentDensityIterator;

public class FragmentSplitter {
	
	private Map<Fragment, Double> varianceReduction;
	private Map<Fragment, Integer> bestSplitPoint;
	
	private double varianceBefore;
	private double varianceAfter;
	
	public static final Logger log = Logger.getLogger("FragmentSplitter");
	
	public static void enableLogging() {
		log.setLevel(Level.ALL);
	}
	
	public static void disableLogging() {
		log.setLevel(Level.OFF);
	}
	
	public FragmentSplitter() {

	}
		
	public List<Fragment> pickBestFragmentSplit(DensityEstimator de, List<Fragment> fragments) {
		log.info("Evaluating fragments: " + fragments);
		varianceReduction = new HashMap<>();
		bestSplitPoint = new HashMap<>();
		varianceBefore = 0;
		varianceAfter = 0;
		
		Iterator<List<Integer>> fdi = new FragmentDensityIterator(fragments, de);
		Iterator<Fragment> it = fragments.iterator();
		
		iterateSimultaneously(it, fdi, this::storeBestSplitForFragment);
		
		// see if any of our split points create a reduction in variance
		// and find the best one.
		Fragment bestFragToSplit = varianceReduction.entrySet().stream()
				.filter(e -> e.getValue() > 0.001)
				.max((a, b) -> (int) Math.signum(a.getValue() - b.getValue()))
				.map(e -> e.getKey())
				.orElse(null);
		
		if (bestFragToSplit == null) {
			varianceAfter = varianceBefore;
			return fragments; // don't do a split
		}
		
		// we need to do a split.
		List<Fragment> newFrags = bestFragToSplit.split(bestSplitPoint.get(bestFragToSplit));
		
		List<Fragment> toR = new ArrayList<>(fragments.size() + 1);
		toR.addAll(fragments);
		
		toR.remove(bestFragToSplit);
		toR.addAll(newFrags);
		Collections.sort(toR);
		
		varianceAfter = varianceBefore - varianceReduction.get(bestFragToSplit);
		
		//System.out.println("Reduced variance by " + varianceReduction.get(bestFragToSplit));
		
		return toR;
	}
	
	public double getVarianceBefore() {
		return varianceBefore;
	}
	
	public double getVarianceAfter() {
		return varianceAfter;
	}
	
	private void storeBestSplitForFragment(Fragment f, List<Integer> densities) {
		
		
		SplitData sd = findSplitPoint(f, densities);
		varianceBefore += sd.varianceBeforeSplit;
		varianceReduction.put(f, sd.varianceBeforeSplit - sd.varianceAfterSplit);
		bestSplitPoint.put(f, sd.splitPoint);

		
	
	}
	
	static SplitData findSplitPoint(Fragment f, List<Integer> densities) {
		// for now, we'll do the naive, inefficient method. We can use the 
		// decision tree split finding algorithm to do this in O(n) instead of
		// O(n^2) later.
		
		double[] values = densities.stream()
				.mapToDouble(i -> i)
				.toArray();
		
		SumOfSquares v = new SumOfSquares();
		double totalVariance = v.evaluate(values);
		
		
		double[] splitPointVariance = new double[values.length];
	
		
		for (int i = 0; i < splitPointVariance.length; i++) {
			// consider a split at point i, where i is on the left
			// and the rest of the array is on the right.
			double rightSideVariance = v.evaluate(Arrays.copyOfRange(values, i+1, values.length));
			double leftSideVariance = v.evaluate(Arrays.copyOfRange(values, 0, i+1));
			
			if (i < 20 || values.length - i < 20) {
				splitPointVariance[i] = Double.POSITIVE_INFINITY;
			} else {
				splitPointVariance[i] = rightSideVariance + leftSideVariance;
			}
			
			if (i % 25 == 0 && i > 25) {
				log.info(Arrays.toString(Arrays.copyOfRange(values, i-25, i+1)));
				log.info("At " + i + ": " + leftSideVariance + " , " + rightSideVariance + " , " + (leftSideVariance + rightSideVariance));
			}
			
		}
		
		
		int bestSplit = 0;
		for (int i = 1; i < splitPointVariance.length; i++) {
			if (splitPointVariance[i] < splitPointVariance[bestSplit])
				bestSplit = i;
		}
		
		//System.out.println("Original variance: " + totalVariance + " best split: " + bestSplit
		//		+ " with new variance sum: " + splitPointVariance[bestSplit]);
		
		SplitData toR = new SplitData();
		toR.varianceBeforeSplit = totalVariance;
		toR.varianceAfterSplit = splitPointVariance[bestSplit];
		toR.splitPoint = bestSplit;
		
		log.info("Splitting " + f + " gives " + toR);

		
		return toR;
	}
	
	static class SplitData {
		int splitPoint;
		double varianceBeforeSplit;
		double varianceAfterSplit;
		
		@Override
		public String toString() {
			return "(pt: " + splitPoint + " before: " + varianceBeforeSplit + " after: " + varianceAfterSplit + ")";
		}
	}
	
	public static void main(String[] args) {
		Fragment f1 = new Fragment(0, 10);
		Fragment f2 = new Fragment(10, 20);
		Fragment f3 = new Fragment(20, 30);
		
		List<Fragment> fragments = new ArrayList<>();
		fragments.add(f1);
		fragments.add(f2);
		fragments.add(f3);
		
		DensityEstimator de = new DensityEstimator();
		de.addQuery(new Query(0, 5));
		de.addQuery(new Query(7, 15));
		de.addQuery(new Query(5, 30));
		
		FragmentSplitter fs = new FragmentSplitter();
		
		List<Fragment> newFrags = fs.pickBestFragmentSplit(de, fragments);
		System.out.println(fragments);
		System.out.println(newFrags);
		System.out.println("before: " + fs.getVarianceBefore() + " after: " + fs.getVarianceAfter());
	}
	
	
	// http://stackoverflow.com/a/37612232/1464282 (modified for iterators)
	static <T1, T2> void iterateSimultaneously(Iterator<T1> c1, Iterator<T2> c2, BiConsumer<T1, T2> consumer) {
	    Iterator<T1> i1 = c1;
	    Iterator<T2> i2 = c2;
	    while (i1.hasNext() && i2.hasNext()) {
	        consumer.accept(i1.next(), i2.next());
	    }
	}
	
}
