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
import java.util.List;

public class Fragment implements Comparable<Fragment> {
	private int start;
	private int stop;
	
	private double expectedProfit;
	
	public Fragment(int start, int stop) {
		this.start = start;
		this.stop = stop;
		
		if (this.start == this.stop)
			throw new IllegalArgumentException("Cannot create a fragment with zero size!");
		
		if (this.start > this.stop)
			throw new IllegalArgumentException("Start cannot be less than stop!");
	}
	
	public Fragment(edu.brandeis.nashdb.Fragment f) {
		this(f.getStart(), f.getStop());
	}
	
	public void setExpectedProfit(double p) {
		this.expectedProfit = p;
	}
	
	public double getExpectedProfit() {
		return expectedProfit;
	}
	
	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}

	public int compareTo(Fragment arg0) {
		if (start != arg0.start)
			return start - arg0.start;
		return stop - arg0.stop;
	}
	
	public List<Fragment> split(int point) {
		List<Fragment> toR = new ArrayList<>(2);
		
		toR.add(new Fragment(start, point + start + 1));
		toR.add(new Fragment(point + start + 1, stop));
		
		return toR;
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(start * stop);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Fragment))
			return false;
		
		Fragment other = (Fragment) o;
		return other.start == start && other.stop == stop;
	}
	
	@Override
	public String toString() {
		return "F[" + this.start + ", " + this.stop + "]";
	}

	public int getSize() {
		return this.stop - this.start;
	}

	public boolean contains(int tuple) {
		return tuple >= getStart() && tuple < getStop();
	}
	
	
}
