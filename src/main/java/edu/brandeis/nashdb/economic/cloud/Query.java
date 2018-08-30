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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.brandeis.nashdb.economic.fragments.Fragment;

public class Query {
	private int start;
	private int stop;
	private int value;
	private final Set<Subquery> subqueries;
	private Subquery biggestSQ;

	public Query(int start, int stop, int value) {
		if (start < 0 || stop < 0 || start == stop || start > stop)
			throw new IllegalArgumentException("Query boundries must be natural numbers and not equal to each other."
					+ " Got: " + start + " to " + stop);

		if (value <= 0)
			throw new IllegalArgumentException("Query value must be a natural number.");

		this.start = start;
		this.stop = stop;
		this.value = value;
		subqueries = new HashSet<>();
	}
	
	public Query(int start, int stop) {
		this(start, stop, 1);
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

	void noteSubquery(Subquery sq) {
		subqueries.add(sq);
	}
	
	public int getQuerySpan() {
		return subqueries.stream()
				.map(sq -> sq.getAssignedVM())
				.collect(Collectors.toSet()).size();
	}


	public int getFinishTime() {
		int toR = subqueries.stream()
				.max((a, b) -> a.finishedAt() - b.finishedAt())
				.get().finishedAt();
		
		
		return toR;
	}

	@Override
	public String toString() {
		return "Q[" + start + ", " + stop + "]";
	}

	public Collection<Fragment> getRelevantFragments(Collection<Fragment> fragments) {
		List<Fragment> toR = new LinkedList<>();
		for (Fragment f : fragments) {
			if (f.getStop() < getStart())
				continue;
			
			if (f.getStart() > getStop())
				continue;
			
			toR.add(f);
				
		}
		return toR;
	}

	public static Query tweakEndpoints(int start, int stop, int max) {
		int v1 = Math.max(0, start);
		int v2 = Math.max(0, stop);

		v1 = Math.min(v1, max);
		v2 = Math.min(v2, max);

		if (v1 == 0 && v2 == 0) {
			v1 = 0;
			v2 = 1;
		}

		if (v1 == v2) {
			v1--;
		}

		return new Query(v1, v2);

	}

	public int getSize() {
		return getStop() - getStart();
	}

	public void reset() {
		subqueries.clear();
		biggestSQ = null;
	}

	public Subquery getBiggestSubquery() {
		if (biggestSQ == null)
			biggestSQ = subqueries.stream()
			.filter(sq -> sq.isBiggest())
			.findFirst().get();
		
		return biggestSQ;
	}

}
