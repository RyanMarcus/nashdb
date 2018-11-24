package edu.brandeis.nashdb.examples;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;

import edu.brandeis.nashdb.Fragment;
import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;
import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.util.RenderExample;
import info.rmarcus.JsonArrayCollector;

public class Scn2JSONFormattor {
	
	private static final int NUM_TUPLES = 100;
	private static final int NUM_FRAGMENTS = 5;
	private static final int TIMESTAMPS_PER_TRANSITION = 10;
	private static final Random r = new Random(43);
	private static final Query[] queries = generateQueries(6);
	private static int MAX_FRAGS_IN_VM = 0;
	
	//x6
	private static Query[] generateQueries(int numQueries) {
		Query[] queryHolder = new Query[numQueries];
		
		for (int i = 0; i < numQueries; i++) {
			int v1 = r.nextInt(NUM_TUPLES);
			int v2 = r.nextInt(NUM_TUPLES);
			
			if (v1 == v2) v2++;
						
			queryHolder[i] = new Query(Math.min(v1, v2), Math.max(v1, v2));
		}
		return queryHolder;
	}
	
	
	// parse out the number of queries
	// parse out the budget for each query
	// construct a new nashdb object
	// call notequery a bunch of times
	// call toJSON
	// spit that back to the client 
	public static String generateJSON(String queryVolume, String queryBudget) {
		JSONParser JSONparser = new JSONParser();
		JSONArray qvArray = new JSONArray();
		JSONArray qbArray = new JSONArray();
		
		try {
			qvArray = (JSONArray) JSONparser.parse(queryVolume);
			qbArray = (JSONArray) JSONparser.parse(queryVolume);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("could not parsed");
			return "ERROR";//return an error
		}
		if (qvArray.size() != qbArray.size()) return "ERROR";//return an error
		NashDB nash = NashDB.freshNashDBInstance(NUM_TUPLES);

		for (int i = 0; i < qvArray.size(); i++) {
			for (int j = 0; j < Integer.parseInt((String)qvArray.get(i)); j++) {
				nash.noteQuery(queries[i].getStart(), queries[i].getStop(), Integer.parseInt((String)qbArray.get(i)));
			}
		}
		return createJSON(nash);
	}
	public static String createJSON(NashDB nash) {
		JsonArray output = new JsonArray();
		
		Collection<Collection<Fragment>> currentConfiguration = null;

		JsonObject dataForTimestamp = new JsonObject();
		// add the data for the tuple value graph to the JSON output
		dataForTimestamp.add("graph values", 
				nash.getEstimatedTupleValues().stream()
				.map(rav -> rav.toJSON())
				.collect(new JsonArrayCollector()));
		
		
		// time to do a transition. compute new fragments
		List<Fragment> fragments = new ArrayList<>(nash
				.doFragmentation(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS)
				.getFragments());
		Collections.sort(fragments);
		   
		// add the fragment data to the JSON output
		dataForTimestamp.add("fragments",
				fragments.stream()
				.map(f -> f.toJSON(nash))
				.collect(new JsonArrayCollector()));

		// allocate the fragments onto VMs and build a JSON structure mapping each VM to
		// a fragment index.
		Collection<Collection<Fragment>> vms = nash.doFragmentAllocation(fragments);
		currentConfiguration = vms;
		JsonArray jsonVMs = vms.stream()
				.map(vm -> RenderExample.fragmentsToIndexes(vm, fragments))
				.map(idxs -> idxs.stream().map(i -> Json.value(i)).collect(new JsonArrayCollector()))
				.collect(new JsonArrayCollector());
		dataForTimestamp.add("vms", jsonVMs);

		
		// compute the profit for each fragment
		SortedMap<Fragment, Double> profits = new TreeMap<>(
				nash.calculateFragmentProfits(currentConfiguration));
		JsonArray jsonProfits = new JsonArray();
		profits.values().forEach(jsonProfits::add);
		dataForTimestamp.add("fragment profits", jsonProfits);
		
		// compute the ideal deltas for each fragment
		SortedMap<Fragment, Double> deltas = new TreeMap<>(
				nash.calculateIdealDeltas(currentConfiguration));
		JsonArray jsonDeltas = new JsonArray();
		deltas.values().forEach(jsonDeltas::add);
		dataForTimestamp.add("fragment deltas", jsonDeltas);
		
		output.add(dataForTimestamp);
		
		JsonObject wrapper = new JsonObject();
		wrapper.add("data", output);
		System.out.println(wrapper.toString());
		return wrapper.toString();
	}
}
	

