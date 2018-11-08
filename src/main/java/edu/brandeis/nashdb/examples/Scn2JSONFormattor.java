package edu.brandeis.nashdb.examples;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;
import edu.brandeis.nashdb.economic.cloud.Query;

public class Scn2JSONFormattor {
	
	private static final int NUM_TUPLES = 100;
	private static final int NUM_FRAGMENTS = 5;
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
		if (queryVolume != queryBudget) System.out.println("error");//return an error
		JSONParser JSONparser = new JSONParser();
		JSONArray qvArray = new JSONArray();
		JSONArray qbArray = new JSONArray();
		
		try {
			qvArray = (JSONArray) JSONparser.parse(queryVolume);
			qbArray = (JSONArray) JSONparser.parse(queryVolume);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NashDB nash = NashDB.freshNashDBInstance(NUM_TUPLES);
		for (int i = 0; i < qvArray.size(); i++) {
			for (int j = 0; j < ((Long) qvArray.get(i)).intValue(); j++) {
				nash.noteQuery(queries[i].getStart(), queries[i].getStop(), ((Long) qbArray.get(i)).intValue());
				
			}
		}
		return createJSON(nash);
	}
	public static String createJSON(NashDB nash) {
		String stringToParse = nash.toJSON(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS);
		JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();
		
		try {
			json = (JSONObject) parser.parse(stringToParse);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray seriesArray = new JSONArray();
		
		JSONArray fragments_fromJSON =  (JSONArray) json.get("fragments");
		JSONArray vms_fromJSON = (JSONArray) json.get("vms");
		JSONArray graphData_fromJSON = (JSONArray) json.get("graphData");
		GraphDataObj[] graphDataArray = JSONFormatter.returnGraphDataArray(graphData_fromJSON);
		
		TreeMap<Integer, Integer> colorMap = new TreeMap<Integer, Integer>();
		TreeSet<Integer> usedColorIndexes = new TreeSet<Integer>();

		JSONArray vms_object = JSONFormatter.assignFragmentsToVMS(fragments_fromJSON, vms_fromJSON, colorMap, usedColorIndexes, graphDataArray);
		JSONArray f = JSONFormatter.returnFragmentArray(graphDataArray, fragments_fromJSON, colorMap, usedColorIndexes);
		JSONArray transitionInfo = new JSONArray();
		transitionInfo = JSONFormatter.returnedTransitionArray(nash, vms_fromJSON, null, fragments_fromJSON);
		JSONObject seriesUnit = new JSONObject();
		seriesUnit.put("tuples", json.get("graphData"));
		seriesUnit.put("fragments", f);
		seriesUnit.put("vms", vms_object);
		seriesUnit.put("transition", transitionInfo);
		seriesArray.add(seriesUnit);
		JSONObject returnedJSON = new JSONObject();
		returnedJSON.put("maxFragmentsInVms", MAX_FRAGS_IN_VM);
		returnedJSON.put("series", seriesArray);
		
		return returnedJSON.toJSONString();
	}
	
}
