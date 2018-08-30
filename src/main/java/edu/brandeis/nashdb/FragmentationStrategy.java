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

/**
 * Indicates which of two clustering algorithms to use for a
 * particular task. The optimal algorithm runs in O(n^3) time,
 * and the greedy algorithm runs in O(n) time.
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public enum FragmentationStrategy {
	OPTIMAL, GREEDY;
}
