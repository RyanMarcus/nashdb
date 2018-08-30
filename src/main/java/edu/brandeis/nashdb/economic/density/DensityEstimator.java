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
 
package edu.brandeis.nashdb.economic.density;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.fragments.Fragment;

import java.util.NavigableMap;
import java.util.TreeMap;

public class DensityEstimator {
	
	final int BOUND;
	
	private NavigableMap<Integer, RangeEndpoint> data;
	private Deque<Query> contained;
	private boolean bounded = false;
	
	private double[] sumCache;
	private double[] sumSqCache;
	
	public DensityEstimator(int bound) {
		data = new TreeMap<>();
		contained = new LinkedList<>();
		this.BOUND = bound;
		
		this.bounded = bound != 0;
	}
	
	public DensityEstimator() {
		this(0);
	}
	
	public void addQuery(Query q) {
		invalidateCache();
		RangeEndpoint re = data.get(q.getStart());
		if (re != null) {
			re.addStarting(q.getValue());
		} else {
			RangeEndpoint toAdd = new RangeEndpoint();
			toAdd.addStarting(q.getValue());
			data.put(q.getStart(), toAdd);
		}
		
		re = data.get(q.getStop());
		if (re != null) {
			re.addEnding(q.getValue());
		} else {
			RangeEndpoint toAdd = new RangeEndpoint();
			toAdd.addEnding(q.getValue());
			data.put(q.getStop(), toAdd);
		}
		
		contained.add(q);
		if (bounded) {
			checkQueueBound();
		}
	}
	
	private void checkQueueBound() {
		invalidateCache();

		while (contained.size() > BOUND) {
			Query toRemove = contained.pop();
			RangeEndpoint re = data.get(toRemove.getStart());
			if (re.removeStarting(toRemove.getValue())) {
				// remove the whole entry from the tree
				data.remove(toRemove.getStart());
			}
			
			re = data.get(toRemove.getStop());
			if (re.removeEnding(toRemove.getValue())) {
				// remove the whole entry from the tree
				data.remove(toRemove.getStop());
			}
		}
	}
	
	private void invalidateCache() {
		sumCache = null;
		sumSqCache = null;
	}
	
	public List<Integer> getDensityInFragment(Fragment f) {
		List<Integer> l = getFlatDensity(f.getStop());
		
		if (l.size() < f.getStart())
			return new ArrayList<Integer>();
		
		return l.subList(f.getStart(), Math.min(l.size(), f.getStop()));
				
	}
	
	public DensityIterator iterator() {
		return new DensityIterator();
	}
	
	public List<Integer> getFlatDensity(int end) {
		List<Integer> toR = new ArrayList<>();
		
		DensityIterator di = iterator();
		
		while (di.hasNext()) {
			for (int i = di.getCurrentPoint();
					i < Math.min(end, di.getCurrentEnd());
					i++) {
				
				while (toR.size() < i)
					toR.add(0);
				
				toR.add(di.getCount());
			}
			
			di.next();
		}
		
		while (toR.size() < end)
			toR.add(0);
		
		return toR;
	}
	
	public void precomputeWithinness(int numTuples) {
		int n = numTuples;
		sumCache = new double[n];
		sumSqCache = new double[n];
		
		// each cache array holds the sum or squared sum
		// of all elements from i to j.
		List<Integer> values = getFlatDensity(n);
		
		sumCache[0] = values.get(0);
		sumSqCache[0] = sumCache[0] * sumCache[0];
		
		for (int i = 1; i < n; i++) {
			sumCache[i] = sumCache[i-1] + values.get(i);
			sumSqCache[i] = sumSqCache[i-1] + 
					Math.pow(values.get(i), 2);
			
		}
		
	}
	
	public double getWithinness(int start, int stop) {
		if (start == stop)
			return 0;
		
		double sum, sumSq;
		if (sumCache != null && sumSqCache != null) {
			// we can use the cache!
			sum = sumCache[stop-1] - (start > 0 ? sumCache[start-1] : 0);
			sumSq = sumSqCache[stop-1] - (start > 0 ? sumSqCache[start-1] : 0);
		} else {
			DensityIterator di = iterator();

			sum = 0.0;
			sumSq = 0.0;

			while (di.hasNext()) {
				if (di.getCurrentEnd() < start) {
					di.next();
					continue;
				}

				if (di.getCurrentPoint() > stop)
					break;

				// compute the length of this segment that is
				// in range.
				int a = Math.max(di.getCurrentPoint(), start);
				int b = Math.min(di.getCurrentEnd(), stop);
				int count = b - a;

				sum += count * di.getCount();
				sumSq += count * Math.pow(di.getCount(), 2);
				di.next();
			}
			
		}
		// var = E(x^2) - E(x)^2


		double range = stop - start;
		double var = sumSq / range;
		var -= Math.pow(sum / range, 2);
		var *= range;
		
		return var;
	}
	
	public class DensityIterator {
		private Iterator<Entry<Integer, RangeEndpoint>> it = data.entrySet().iterator();
		private int count = 0;
		private int currentPoint = 0;
		private Integer currentEnd = 0;
		
		public DensityIterator() {
			next();
		}
		
		public void next() {
			Entry<Integer, RangeEndpoint> re = it.next();
			count += re.getValue().numIntervalsStarting;
			count -= re.getValue().numIntervalsEnding;
			
			currentPoint = re.getKey();
			currentEnd = data.higherKey(re.getKey());
		}
		
		public boolean hasNext() {
			return it.hasNext();
		}
		
		public int getCount() {
			return count;
		}
		
		public int getCurrentPoint() {
			return currentPoint;
		}
		
		public int getCurrentEnd() {
			return (currentEnd == null ? Integer.MAX_VALUE : currentEnd);
		}
	}
	
	public Collection<Query> getQueries() {
		List<Query> toR = new ArrayList<>(contained.size());
		
		for (Query q : contained) {
			Query toAdd = new Query(q.getStart(), q.getStop());
			toR.add(toAdd);
		}
		return Collections.unmodifiableCollection(toR);
	}
	
	public int getEnd() {
		return data.lastKey();
	}
	
	public static void main(String[] args) {
		DensityEstimator de = new DensityEstimator();
//		de.addQuery(new Query(0, 10));
//		de.addQuery(new Query(5, 12));
//		de.addQuery(new Query(0, 10));
//		de.addQuery(new Query(0, 100));
//		de.addQuery(new Query(20, 30));
//		
		de.addQuery(new Query(0, 5));
		de.addQuery(new Query(5, 15));
		de.addQuery(new Query(5, 30));
		
		DensityIterator di = de.iterator();
	
		
		while (di.hasNext()) {
			System.out.println(di.getCount() + " at " + di.getCurrentPoint() + " ending at " + di.getCurrentEnd());
			di.next();
		}
		System.out.println(di.getCount() + " at " + di.getCurrentPoint() + " ending at " + di.getCurrentEnd());

	}

}
