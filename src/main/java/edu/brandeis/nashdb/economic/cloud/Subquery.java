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
 
package edu.brandeis.nashdb.economic.cloud;

import edu.brandeis.nashdb.economic.fragments.Fragment;

public class Subquery {
	private final Fragment relevantFrag;
	private final Query query;
	private int finishedAt;
	private boolean biggest;
	private VM assignedVM;
	
	public Subquery(Fragment relevantFrag, Query parent, VM assigned) {
		this.relevantFrag = relevantFrag;
		this.query = parent;
		this.assignedVM = assigned;
		query.noteSubquery(this);
	}
	
	public Query getQuery() {
		return query;
	}
	
	public Fragment getRelevantFragment() {
		return relevantFrag;
	}
	
	public int getSize() {
		return Math.min(query.getStop(), relevantFrag.getStop()) 
				- Math.max(query.getStart(), relevantFrag.getStart());
	}
	
	int finishedAt() {
		return finishedAt;
	}
	
	void setFinishedAt(int finishedAt) {
		this.finishedAt = finishedAt;
	}
	
	@Override
	public String toString() {
		return relevantFrag + " of " + query;
	}

	public void markBiggest() {
		this.biggest = true;
	}
	
	public boolean isBiggest() {
		return this.biggest;
	}
	
	public VM getAssignedVM() {
		return assignedVM;
	}
}
