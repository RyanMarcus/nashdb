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
import java.util.Set;
import java.util.stream.Collectors;

public class StaticScheduleEvaluator {

	
	public static Cost getCost(Collection<VM> vms) {
		Cost toR = new Cost();
		
		
//		toR.cost = vms.size() * VM.COST * 500;
//		for (VM m : vms) {
//			toR.cost += m.getRunningTime() * VM.COST;
//		}
		
		Set<Query> allQueries = collectQueries(vms);
		
		long maxLatency = allQueries.stream()
				.max((a, b) -> a.getFinishTime() - b.getFinishTime())
				.get().getFinishTime();
				
		toR.latency = (int) allQueries.stream()
				.mapToDouble(q -> q.getFinishTime())
				.average().orElse(0);
		
		toR.totalVMTime = vms.stream()
				.mapToLong(m -> m.getRunningTime()).sum();
				

		toR.averageQuerySpan = allQueries.stream()
				.mapToDouble(q -> q.getQuerySpan())
				.average().getAsDouble();
		
		
		toR.numVMs = vms.size();
				
//		System.out.println("max latency is: " + maxLatency);
//		System.out.println("num VMs: " + vms.size());
//		System.out.println("Total VM time: " + (maxLatency * vms.size()) + " min: " + (minLatency * vms.size()) + " diff: " + (maxLatency - minLatency));
		
		toR.cost = vms.size() * (VM.STARTUP_COST)
				//+ toR.totalVMTime * VM.COST;
				+ maxLatency * vms.size() * VM.COST;
		
		toR.startupCost = vms.size() * (VM.STARTUP_COST);
		toR.costWithoutStartup = maxLatency * vms.size() * VM.COST;
		
		return toR;
	}
	
	private static Set<Query> collectQueries(Collection<VM> vms) {
		Set<Query> toR = new HashSet<>();
		
		for (VM vm : vms) {
			toR.addAll(vm.getSubqueries().stream()
					.map(sq -> sq.getQuery())
					.collect(Collectors.toList()));
		}
		
		return toR;
	}
	
	public static class Cost {
		public long costWithoutStartup;
		public int startupCost;
		public double averageQuerySpan;
		public int latency;
		public long cost;
		public long numVMs;
		public long totalVMTime;
		public int networkTransitionCost;
		
		@Override
		public String toString() {
			return String.format("[l: %d c: %d s: %f m: %d]", latency, cost, averageQuerySpan, numVMs);
		}
		
	}
}
