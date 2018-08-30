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
 
package edu.brandeis.nashdb.economic.experiments;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.fragments.Fragment;
import edu.brandeis.nashdb.economic.fragments.SumOfSquares;

public class CalcUtils {

	public static double computeVarianceSum(DensityEstimator de, List<Fragment> fragments) {
		double sum = 0.0;
		for (Fragment f : fragments) {
			double thisFrag = de.getWithinness(f.getStart(), f.getStop());
			sum += thisFrag;
		}
		
		return sum;
	}

	public static double computeVarianceSumOfDisjoint(DensityEstimator de, Collection<Set<Fragment>> fragments) {
		SumOfSquares v = new SumOfSquares();
		double sum = 0.0;
		
		for (Set<Fragment> fgs : fragments) {
			double[] values = fgs.stream()
					.flatMap(f -> de.getDensityInFragment(f).stream())
					.mapToDouble(i -> i).toArray();
			
			sum += v.evaluate(values);
		}
		
		return sum;
	}
	

}
