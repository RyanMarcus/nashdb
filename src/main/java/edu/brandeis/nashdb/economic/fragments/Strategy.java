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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.brandeis.nashdb.economic.density.DensityEstimator;

public interface Strategy {

	public List<Fragment> executeRound(DensityEstimator de, Collection<Fragment> fragments);

	
	public static Collection<Fragment> splitLarge(int maxSize, Collection<Fragment> frags) {
		if (frags.stream().allMatch(f -> f.getSize() <= maxSize))
			return frags;
		
		Set<Fragment> toR = new HashSet<>();
		for (Fragment f : frags) {
			if (f.getSize() > maxSize) {
				toR.add(new Fragment(f.getStart(), f.getStart() + f.getSize() / 2));
				toR.add(new Fragment(f.getStart() + f.getSize() / 2, f.getStop()));
			} else {
				toR.add(f);
			}
		}
		
		return splitLarge(maxSize, toR);
	}
}