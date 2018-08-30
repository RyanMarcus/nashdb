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

class RangeEndpoint {
	int numIntervalsStarting;
	int numIntervalsEnding;
		
	public void addStarting(int val) {
		numIntervalsStarting += val;
	}
	
	public void addEnding(int val) {
		numIntervalsEnding += val;
	}
	
	public boolean removeStarting(int val) {
		numIntervalsStarting -= val;
		if (numIntervalsStarting < 0) {
			numIntervalsStarting = 0;
			throw new IllegalStateException("Attempted to decrement the number of starting intervals below 0");
		}
		return numIntervalsStarting <= 0 && numIntervalsEnding <= 0;
	}
	
	public boolean removeEnding(int val) {
		numIntervalsEnding -= val;
		if (numIntervalsEnding < 0) {
			numIntervalsEnding = 0;
			throw new IllegalStateException("Attempted to decrement the number of ending intervals below 0");
		}
		return numIntervalsStarting <= 0 && numIntervalsEnding <= 0;
	}

	
	@Override
	public String toString() {
		return "(starting: " + numIntervalsStarting + ", ending: " + numIntervalsEnding + ")";
	}
	
}