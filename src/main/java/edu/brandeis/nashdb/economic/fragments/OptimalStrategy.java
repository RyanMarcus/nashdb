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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.density.DensityEstimator.DensityIterator;

public class OptimalStrategy implements Strategy {

	private final int numFrags;
	private final int n;
	
	public OptimalStrategy(int numFrags, int numTuples) {
		this.numFrags = numFrags;
		this.n = numTuples;
	}
	
	@Override
	public List<Fragment> executeRound(DensityEstimator de, Collection<Fragment> fragments) {
		// For details, see:
		// Mahlknecht, Dignös, and Gamper, 
		// “A Scalable Dynamic Programming Scheme for the Computation of Optimal k -Segments for Ordered Data.”
		
		de.precomputeWithinness(n);
		
		// the matrix E stores, at i,j, the optimal
		// error for merging the first j input tuples into i
		// fragments. 
		double[][] E = new double[numFrags][n];
		
		// the matrix J stores, at i,j, the (i-1)th split point
		// of the optimal solution for merging the first 
		// j tuples into i fragments
		int[][] J = new int[numFrags][n];
		
		// first, populate the 1st row of E
		for (int j = 0; j < n; j++) {
			E[0][j] = de.getWithinness(0, j);
			J[0][j] = 0;
		}
		
		for (int i = 1; i < numFrags; i++) {
			E[i][i] = 0; // the error of merging i tuples into i fragments is zero
			J[i][i] = i - 1; // the 2nd to the last merge point when everything is in its own fragment
			for (int j = i + 1; j < n; j++) {
				E[i][j] = Double.POSITIVE_INFINITY;

				for (int x = j - 1; x > i-1; x--) {
					double withinness = de.getWithinness(x, j);
					double se = E[i-1][x] + withinness;
					if (se < E[i][j]) {
						E[i][j] = se;
						J[i][j] = x;
					}
					
					if (E[i][j] < withinness)
						break;
				}
			}		
		}
		
		// Z holds the list of optimal split points
		List<Integer> Z = new ArrayList<Integer>(numFrags + 1);
		int j = n-1;
		for (int i = numFrags-1; i > 0; i--) {
			int s = J[i][j];
			Z.add(s);
			j = s;
		}
		
		Collections.reverse(Z);
		
		// transform the list of split points into fragments.
		List<Fragment> toR = new ArrayList<>(numFrags);
		for (int i = 0; i < numFrags; i++) {
			int start = (i == 0 ? 0 : Z.get(i-1));
			int end = (i == numFrags - 1 ? n : Z.get(i));
			
			Fragment f = new Fragment(start, end);
			toR.add(f);
		}
				
		return toR;
	}
	
	public static void main(String[] args) {
		DensityEstimator de = new DensityEstimator();
		de.addQuery(new Query(0, 5));
		de.addQuery(new Query(0, 7));
		de.addQuery(new Query(3, 10));
		
		DensityIterator di = de.iterator();
		
		while (di.hasNext()) {
			System.out.println(di.getCurrentPoint() + " to " + di.getCurrentEnd() + " has " + di.getCount());
			di.next();
		}
		
		System.out.println(de.getFlatDensity(10));
		
		OptimalStrategy os = new OptimalStrategy(2, 10);
		
		System.out.println(os.executeRound(de, null));
		
		
	}

}
