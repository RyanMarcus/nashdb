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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.google.common.collect.Streams;

import edu.brandeis.nashdb.economic.cloud.VM;

public class TransitionCostCalculator {
	
	private WeightedGraph<VMVertex, DefaultWeightedEdge> g;
	private Set<DefaultWeightedEdge> edges;
	
	
	public TransitionCostCalculator(List<VM> before, List<VM> after) {
		// first, compute the number of tuples
		int numTuples = before.stream()
				.mapToInt(vm -> vm.getFragments().stream().mapToInt(f -> f.getStop()).max().orElse(0))
				.max().getAsInt();
		
		g = new SimpleWeightedGraph<VMVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		Set<VMVertex> p1 = Streams.mapWithIndex(
				before.stream(), 
				(itm, idx) -> new VMVertex(idx, itm, numTuples))
				.collect(Collectors.toSet());
		
		Set<VMVertex> p2 = Streams.mapWithIndex(
				after.stream(), 
				(itm, idx) -> new VMVertex(idx, itm, numTuples))
				.collect(Collectors.toSet());
		
		// add dummies
		long dummyIdx = -1;
		while (p1.size() < p2.size() || p2.size() < p1.size()) {
			(p1.size() < p2.size() ? p1 : p2).add(new VMVertex(dummyIdx--, numTuples));
		}
		
		p1.forEach(g::addVertex);
		p2.forEach(g::addVertex);

		for (VMVertex a : p1) {
			for (VMVertex b : p2) {
				DefaultWeightedEdge e = g.addEdge(a, b);
				g.setEdgeWeight(e, a.computeEdgeWeight(b));
			}
		}
		
		KuhnMunkresMinimalWeightBipartitePerfectMatching<VMVertex, DefaultWeightedEdge>
		m = new KuhnMunkresMinimalWeightBipartitePerfectMatching<VMVertex, DefaultWeightedEdge>(g, p1, p2);
		
		edges = m.computeMatching().getEdges();
	}
	
	public double getTransitionCost() {
		return edges.stream()
				.mapToDouble(e -> g.getEdgeWeight(e))
				.sum();
	}
	
	public Map<Long, Long> getTransitionStrategy() {
		return edges.stream()
				.collect(Collectors.toMap(
						e -> g.getEdgeSource(e).getID(),
						e -> g.getEdgeTarget(e).getID()));
	}
	
	private class VMVertex {
		
		public final VM vm;
		public final boolean isDummy;
		private final int numTuples;
		private final long id;
				
		public VMVertex(long id, int numTuples) {
			this.id = id;
			isDummy = true;
			vm = null;
			this.numTuples = numTuples;
		}
	
		public VMVertex(long id, VM vm, int numTuples) {
			this.id = id;
			this.vm = vm;
			isDummy = false;
			this.numTuples = numTuples;
		}
		
		public long getID() {
			return id;
		}
		
		public int computeEdgeWeight(VMVertex other) {
			if (isDummy && other.isDummy)
				throw new IllegalStateException("Cannot compute edge weight between two dummy nodes!");
			
			if (isDummy) {
				// I'm being newly created
				return other.vm.getUsedSpace();
			}
			
			if (other.isDummy) {
				// I'm being destroyed
				return 0; 
			}
			
			int uncovered = 0;
			// otherwise, we need to compute the non-overlapping parts of the fragments
			for (int i = 0; i < numTuples; i++) {
				if (other.vm.isTupleCovered(i) && !vm.isTupleCovered(i))
					uncovered++;
			}
			
			return uncovered;
		}
	}
}
