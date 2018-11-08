package edu.brandeis.nashdb.examples;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;
import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.economic.fragments.OptimalStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.TreeSet;

public class createNashJSON {
	private static final int NUM_TUPLES = 100;
	private static final int NUM_FRAGMENTS = 5;
	private static int MAX_FRAGS_IN_VM = 0;

	public static void main(String[] args) {
		 System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));	}
//		JSONFormatter formatter = new JSONFormatter();
//		
//		JSONParser JSONparser = new JSONParser();
//		System.out.println("Working Directory = " + System.getProperty("user.dir"));
//		JSONArray jsonInfo = new JSONArray();
//		try {
//			jsonInfo = (JSONArray) JSONparser.parse(new FileReader("/home/g/Desktop/moving_data1.json"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			System.out.println("file not found");
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		NashDB nash = NashDB.freshNashDBInstance(NUM_TUPLES);
////		DensityEstimator de = new DensityEstimator();
////		OptimalStrategy os = new OptimalStrategy(2, 10);
////		for (int i = 0; i < jsonInfo.size(); i++) {
////			JSONArray timeStamp = (JSONArray) jsonInfo.get(0);
////			for (int j= 0; j < timeStamp.size(); j++) {
////				JSONArray query = (JSONArray) timeStamp.get(0);
////				de.addQuery(new Query(((Long) query.get(0)).intValue(), ((Long) query.get(1)).intValue()));
////			}
////			os.executeRound(de, null);
////			String stringToParse = nash.toJSON(os, NUM_FRAGMENTS);
////			System.out.println(stringToParse);
////		}
//		JSONArray seriesArray = new JSONArray();
//		JSONArray fragments_fromJSON =  new JSONArray();
//		JSONArray vms_fromJSON = new JSONArray();
//		JSONArray old_vms_fromJSON = new JSONArray();
//		JSONArray graphData_fromJSON = new JSONArray();
//		GraphDataObj[] graphDataArray = null;
//
//		 
//		JSONArray finalOutput = new JSONArray();
//		for (int i = 1; i <= 12; i++) {
//			JSONArray timeStamp = (JSONArray) jsonInfo.get(i);
//			
//			for (int j = 0; j < timeStamp.size(); j++) {
//				JSONArray query = (JSONArray) timeStamp.get(j);
//				int queryStart = ((Long) query.get(0)).intValue();
//				int queryEnd = ((Long) query.get(1)).intValue();
//				if (queryStart == queryEnd) queryEnd++;
//				nash.noteQuery(queryStart, queryEnd, 1);
//			}
//			
//			String stringToParse = nash.toJSON(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS);
//			JSONParser parser = new JSONParser();
//			JSONObject json = new JSONObject();
//			try {
//				json = (JSONObject) parser.parse(stringToParse);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			fragments_fromJSON =  (JSONArray) json.get("fragments");
//			vms_fromJSON = (JSONArray) json.get("vms");
//			TreeMap<Integer, Integer> colorMap = new TreeMap<Integer, Integer>();
//			TreeSet<Integer> usedColorIndexes = new TreeSet<Integer>();
//			graphData_fromJSON = (JSONArray) json.get("graphData");
//			graphDataArray = formatter.returnGraphDataArray(graphData_fromJSON);
//			JSONArray vms_object = formatter.assignFragmentsToVMS(fragments_fromJSON, vms_fromJSON, colorMap, usedColorIndexes, graphDataArray);
//			JSONArray f = formatter.returnFragmentArray(graphDataArray, fragments_fromJSON, colorMap, usedColorIndexes);
//			JSONArray transitionInfo = new JSONArray();
//			if (i == 6 || i == 12) transitionInfo = formatter.returnedTransitionArray(nash, vms_fromJSON, old_vms_fromJSON, fragments_fromJSON);
//			old_vms_fromJSON = vms_fromJSON;
//			JSONObject seriesUnit = new JSONObject();
//			seriesUnit.put("tuples", json.get("graphData"));
//			seriesUnit.put("fragments", f);
//			seriesUnit.put("vms", vms_object);
//			seriesUnit.put("transition", transitionInfo);
//			seriesArray.add(seriesUnit);
//			JSONObject returnedJSON = new JSONObject();
//			returnedJSON.put("maxFragmentsInVms", MAX_FRAGS_IN_VM);
//			returnedJSON.put("series", seriesArray);
//			System.out.println(returnedJSON.toString());
//			
//			finalOutput.add(returnedJSON);
//		}
//		File frontendJSON = new File("/home/g/Desktop/frontendJSON.txt");
//		try (PrintWriter out = new PrintWriter(frontendJSON)) {
//		    out.println(finalOutput.toJSONString());
//		    out.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			System.out.println("file could not be created");
//		}
//		
//	}
}
