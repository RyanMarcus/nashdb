package edu.brandeis.nashdb.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;

import edu.brandeis.nashdb.Fragment;
import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;
import info.rmarcus.JsonArrayCollector;

/**
 * "Renders" the JSON file passed as a command line argument to a JSON file
 * suitable for rendering in our demo.
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class RenderExample {
	
	private static final int TIMESTAMPS_PER_TRANSITION = 10;
	private static final int NUM_FRAGMENTS = 5;
	
	public static void main(String[] args) throws IOException {
		String jsonFilePath = args[0];
		
		FileReader fr = new FileReader(new File(jsonFilePath));
		JsonArray jv = Json.parse(fr).asArray();

		// compute the maximum tuple index, and correct any invalid ranges
		int numTuples = 0;
		for (JsonValue timestamp : jv) {
			JsonArray queriesInTimestamp = timestamp.asArray();
			for (JsonValue query : queriesInTimestamp) {
				int start = query.asArray().get(0).asInt();
				int stop = query.asArray().get(1).asInt();
				
				if (stop > 500)
					stop = 500;
				
				if (start >= 500) {
					start = 499;
					stop = 500;
				}
				
				if (stop <= start) {
					stop = start + 1;
				}
								
				query.asArray().set(0, Json.value(start));
				query.asArray().set(1, Json.value(stop));
				numTuples = Math.max(numTuples, query.asArray().get(1).asInt());
			}
		}
		
		System.out.println("Number of tuples: " + numTuples);
		
		// create a NashDB instance and run the simulation
		NashDB ndb = NashDB.freshNashDBInstance(numTuples);
		JsonArray output = new JsonArray();
		
		Collection<Collection<Fragment>> currentConfiguration = null;
		for (int currentTS = 0; currentTS < jv.size(); currentTS++) {
			JsonObject dataForTimestamp = new JsonObject();
			// note the queries for this timestamp (add them to the tuple value
			// estimator)
			for (JsonValue query : jv.get(currentTS).asArray()) {
				int start = query.asArray().get(0).asInt();
				int end = query.asArray().get(1).asInt();
				ndb.noteQuery(start, end, 1);
			}

			// add the data for the tuple value graph to the JSON output
			dataForTimestamp.add("graph values", 
					ndb.getEstimatedTupleValues().stream()
					.map(rav -> rav.toJSON())
					.collect(new JsonArrayCollector()));
			
			if (currentTS % TIMESTAMPS_PER_TRANSITION == 0) {
				// time to do a transition. compute new fragments
				List<Fragment> fragments = new ArrayList<>(ndb
						.doFragmentation(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS)
						.getFragments());
				Collections.sort(fragments);
				
				// add the fragment data to the JSON output
				dataForTimestamp.add("fragments",
						fragments.stream()
						.map(f -> f.toJSON(ndb))
						.collect(new JsonArrayCollector()));

				// allocate the fragments onto VMs and build a JSON structure mapping each VM to
				// a fragment index.
				Collection<Collection<Fragment>> vms = ndb.doFragmentAllocation(fragments);
				currentConfiguration = vms;
				JsonArray jsonVMs = vms.stream()
						.map(vm -> fragmentsToIndexes(vm, fragments))
						.map(idxs -> idxs.stream().map(i -> Json.value(i)).collect(new JsonArrayCollector()))
						.collect(new JsonArrayCollector());
				dataForTimestamp.add("vms", jsonVMs);
			}
			
			// compute the profit for each fragment
			SortedMap<Fragment, Double> profits = new TreeMap<>(
					ndb.calculateFragmentProfits(currentConfiguration));
			JsonArray jsonProfits = new JsonArray();
			profits.values().forEach(jsonProfits::add);
			dataForTimestamp.add("fragment profits", jsonProfits);
			
			// compute the ideal deltas for each fragment
			SortedMap<Fragment, Double> deltas = new TreeMap<>(
					ndb.calculateIdealDeltas(currentConfiguration));
			JsonArray jsonDeltas = new JsonArray();
			deltas.values().forEach(jsonDeltas::add);
			dataForTimestamp.add("fragment deltas", jsonDeltas);
			
			output.add(dataForTimestamp);
		}
		
		JsonObject wrapper = new JsonObject();
		wrapper.add("data", output);
		FileWriter fw = new FileWriter(new File(args[1]));
		wrapper.writeTo(fw, WriterConfig.PRETTY_PRINT);
		fw.close();
	}
	
	private static List<Integer> fragmentsToIndexes(Collection<Fragment> subset, List<Fragment> allFragments) {
		// note: n^2 approach, could make it faster by building a hashmap from the fragment
		// to its index, but probably not worth it.
		List<Integer> toReturn = subset.stream()
				.map(f -> allFragments.indexOf(f))
				.collect(Collectors.toList());
		Collections.sort(toReturn);
		return toReturn;
				
	}
}
