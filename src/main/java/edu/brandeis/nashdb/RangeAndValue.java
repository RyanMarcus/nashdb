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
 * Represents the semnatic entries of the tuple value estimator. 
 * 
 * Each instance denotes that the tuples between the start and end
 * points (exclusive) have a particular value.
 * 
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class RangeAndValue {
	private int start;
	private int stop;
	private int value;
	
	RangeAndValue(int start, int stop, int value) {
		this.start = start;
		this.stop = stop;
		this.value = value;
	}
	public int getStart() {
		return start;
	}
	public int getStop() {
		return stop;
	}
	public int getValue() {
		return value;
	}
	
	public JsonValue toJSON() {
		JsonObject toReturn = new JsonObject();
		toReturn.add("start", start);
		toReturn.add("end", stop);
		toReturn.add("value", value);
		return toReturn;
	}
}
