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
 
package edu.brandeis.nashdb.economic.schedule;

import java.util.Collection;

import edu.brandeis.nashdb.economic.cloud.VM;
import edu.brandeis.nashdb.economic.fragments.Fragment;

public enum Schedulers {
	MOM, GREEDY;
	
	public Scheduler getScheduler(Collection<Fragment> frags, Collection<VM> vms) {
		switch (this) {
		case MOM:
			return new MaxOfMinScheduler(frags);
		case GREEDY:
			return new GreedyScheduler(frags, vms);
		default:
			break;
		
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
		case GREEDY:
			return "Shortest queue";
		case MOM:
			return "Max of mins";
		default:
			break;
		
		}
		
		return null;
	}
}
