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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public class FragmentDensityIterator implements Iterator<List<Integer>> {
	private Deque<Fragment> dq;
	private List<Integer> flat;
	
	public FragmentDensityIterator(List<Fragment> fragments, DensityEstimator de) {
		dq = new LinkedList<>(fragments);
		flat = de.getFlatDensity(de.BOUND);
	}

	public List<Integer> next() {
		// iterate over the density estimator and create a list of all the density values.
				
		Fragment f = dq.poll();
		return flat.subList(f.getStart(), f.getStop());
	}
	
	public boolean hasNext() {
		return !dq.isEmpty();
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
		de.addQuery(new Query(0, 35));
		de.addQuery(new Query(1, 2));
		de.addQuery(new Query(8, 15));
		
		FragmentDensityIterator fdi = new FragmentDensityIterator(fragments, de);
		
		while (fdi.hasNext()) {
			System.out.println(fdi.next());
		}
	}
}
