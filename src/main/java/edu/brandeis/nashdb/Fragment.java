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
 
package edu.brandeis.nashdb;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Represents a fragment (with a inclusive starting point
 * and an exclusive end point).
 * 
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class Fragment implements Comparable<Fragment> {
	private final int start;
	private final int stop;
	
	public Fragment(int start, int stop) {
		this.start = start;
		this.stop = stop;
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}
	
	public int getSize() {
		return stop - start;
	}
	
	public JsonValue toJSON() {
		JsonObject toReturn = new JsonObject();
		toReturn.add("start", start);
		toReturn.add("end", stop);
		return toReturn;
	}
	
	public JsonValue toJSON(NashDB context) {
		JsonObject toReturn = new JsonObject();
		toReturn.add("start", start);
		toReturn.add("end", stop);
		
		double popularity = context.getTotalFragmentValue(this);
		double mean = popularity / (double)(stop - start);
		toReturn.add("mean value", mean);
		toReturn.add("popularity", popularity);
		
		return toReturn;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Fragment))
			return false;
		
		Fragment o = (Fragment) other;
		return o.start == start && o.stop == stop;
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(start) + Integer.hashCode(stop);
	}

	@Override
	public int compareTo(Fragment arg0) {
		return Integer.compare(this.start, arg0.start);
	}
	
	
	
}
