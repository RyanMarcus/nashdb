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
package edu.brandeis.nashdb.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.brandeis.nashdb.Fragment;
import edu.brandeis.nashdb.FragmentHolder;
import edu.brandeis.nashdb.NashDB;
import edu.brandeis.nashdb.TransitionAction;

/**
 * An example of how to compute the optimal transition policy between two 
 * different fragmentation and replication strategies.
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class ExampleTransition {

	public static class VM implements FragmentHolder {

		private final Collection<Fragment> fragments;
		private final int id;
		
		private VM(int id, Collection<Fragment> fragments) {
			this.id = id;
			this.fragments = fragments;
		}
		
		@Override
		public Collection<Fragment> getFragments() {
			return fragments;
		}
		
		@Override
		public String toString() {
			return "<VM " + id + ">";
		}
		
	}
	
	public static void main(String[] args) {
		List<VM> before = new ArrayList<>();
		List<VM> after = new ArrayList<>();
		
		// Fragmentation & replication scheme before the transition
		before.add(new VM(1, Arrays.asList(new Fragment(0, 10), new Fragment(10, 20), new Fragment(90, 100))));
		before.add(new VM(2, Arrays.asList(new Fragment(20, 30), new Fragment(30, 90))));

		// Target fragmentation & replication scheme
		after.add(new VM(3, Arrays.asList(new Fragment(0, 20))));
		after.add(new VM(4, Arrays.asList(new Fragment(20, 40), new Fragment(40, 90))));
		after.add(new VM(5, Arrays.asList(new Fragment(20, 40), new Fragment(40, 90), new Fragment(90, 100))));

		NashDB nash = NashDB.freshNashDBInstance(100);
		Collection<TransitionAction<VM>> plan = nash.computeTransition(before, after);
		for (TransitionAction<VM> ta : plan) {
			System.out.println(ta);
		}
	}

}
