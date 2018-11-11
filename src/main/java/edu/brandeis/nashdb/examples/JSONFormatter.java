package edu.brandeis.nashdb.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.brandeis.nashdb.Fragment;
import edu.brandeis.nashdb.FragmentHolder;
import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;
import edu.brandeis.nashdb.TransitionAction;
import edu.brandeis.nashdb.economic.cloud.Query;
import edu.brandeis.nashdb.economic.density.DensityEstimator;
import edu.brandeis.nashdb.examples.ExampleTransition.VM;


public class JSONFormatter {
	private NashDB nash;

	public JSONFormatter() {
		this.nash = NashDB.freshNashDBInstance(NUM_TUPLES);
		
	}
	//int numTuples, DensityEstimator de, int vmCost, int vmSize, int timeWindow
//	public JSONFormatter(int numTuples, int vmCost, int vmSize, int timeWindow) {
//		this.nash = new NashDB(numTuples, new DensityEstimator(), vmCost, vmSize, timeWindow);
//	}
	
	public static class VM implements FragmentHolder {
		private final Collection<Fragment> fragments;
		private final int id;
		private VM(int id, Collection<Fragment> fragments) {
			this.id = id;
			this.fragments = fragments;
		}
		public Collection<Fragment> getFragments() {
			return fragments;
		}
		public String toString() {
			return "<VM " + id + ">";
		}
	}

	private static final int NUM_TUPLES = 100;
	private static final int NUM_QUERIES = 500;
	private static final int NUM_FRAGMENTS = 5;
		
	private static int MAX_FRAGS_IN_VM = 0;
	private static final Random r = new Random(43);
	
	/**
	 * Randomly assigned Array of Strings corresponding to HTML Colors
	 */
	private static final String[] colorArray = {"blue", "red", "green", "orange", "purple",
			"pink", "yellow", "maroon", "brown", "turqoise" , "wheat", "tomato", "teal",
			"silver", "rebeccapurple", "olive", "darkgreen"};
	
	
	public void initializeJSON(String jsonName, String newFileName) {
		
		JSONParser JSONparser = new JSONParser();
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		JSONArray jsonInfo = new JSONArray();
		try {
			jsonInfo = (JSONArray) JSONparser.parse(new FileReader(jsonName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("file not found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONArray seriesArray = new JSONArray();
		JSONArray fragments_fromJSON =  new JSONArray();
		JSONArray vms_fromJSON = new JSONArray();
		JSONArray old_vms_fromJSON = new JSONArray();
		JSONArray graphData_fromJSON = new JSONArray();
		GraphDataObj[] graphDataArray = null;

		 
		JSONArray finalOutput = new JSONArray();
		for (int i = 1; i <= 12; i++) {
			JSONArray timeStamp = (JSONArray) jsonInfo.get(i);
			
			for (int j = 0; j < timeStamp.size(); j++) {
				JSONArray query = (JSONArray) timeStamp.get(j);
				int queryStart = ((Long) query.get(0)).intValue();
				int queryEnd = ((Long) query.get(1)).intValue();
				if (queryStart == queryEnd) queryEnd++;
				this.nash.noteQuery(queryStart, queryEnd, 1);
			}
			
			String stringToParse = this.nash.toJSON(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS);
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			
			try {
				json = (JSONObject) parser.parse(stringToParse);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fragments_fromJSON =  (JSONArray) json.get("fragments");
			vms_fromJSON = (JSONArray) json.get("vms");
			graphData_fromJSON = (JSONArray) json.get("graphData");
			graphDataArray = returnGraphDataArray(graphData_fromJSON);
			
			TreeMap<Integer, Integer> colorMap = new TreeMap<Integer, Integer>();
			TreeSet<Integer> usedColorIndexes = new TreeSet<Integer>();

			JSONArray vms_object = assignFragmentsToVMS(fragments_fromJSON, vms_fromJSON, colorMap, usedColorIndexes, graphDataArray);
			JSONArray f = returnFragmentArray(graphDataArray, fragments_fromJSON, colorMap, usedColorIndexes);
			JSONArray transitionInfo = new JSONArray();
			transitionInfo = returnedTransitionArray(this.nash, vms_fromJSON, old_vms_fromJSON, fragments_fromJSON);
			old_vms_fromJSON = vms_fromJSON;
			JSONObject seriesUnit = new JSONObject();
			seriesUnit.put("tuples", json.get("graphData"));
			seriesUnit.put("fragments", f);
			seriesUnit.put("vms", vms_object);
			seriesUnit.put("transition", transitionInfo);
			seriesArray.add(seriesUnit);
			JSONObject returnedJSON = new JSONObject();
			returnedJSON.put("maxFragmentsInVms", MAX_FRAGS_IN_VM);
			returnedJSON.put("series", seriesArray);
			
			finalOutput.add(returnedJSON);
		}
		File frontendJSON = new File(newFileName);
		try (PrintWriter out = new PrintWriter(frontendJSON)) {
		    out.println(finalOutput.toJSONString());
		    out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("file could not be created");
		}
	}
	public void getJSON(String jsonName, String newFileName){
		String stringToParse = this.nash.toJSON(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS);
		JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();
		
		try {
			json = (JSONObject) parser.parse(stringToParse);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray seriesArray = new JSONArray();
		JSONArray fragments_fromJSON =  new JSONArray();
		JSONArray vms_fromJSON = new JSONArray();
		JSONArray old_vms_fromJSON = new JSONArray();
		JSONArray graphData_fromJSON = new JSONArray();
		GraphDataObj[] graphDataArray = null;
		
		fragments_fromJSON =  (JSONArray) json.get("fragments");
		vms_fromJSON = (JSONArray) json.get("vms");
		graphData_fromJSON = (JSONArray) json.get("graphData");
		graphDataArray = returnGraphDataArray(graphData_fromJSON);
		
		TreeMap<Integer, Integer> colorMap = new TreeMap<Integer, Integer>();
		TreeSet<Integer> usedColorIndexes = new TreeSet<Integer>();

		JSONArray vms_object = assignFragmentsToVMS(fragments_fromJSON, vms_fromJSON, colorMap, usedColorIndexes, graphDataArray);
		JSONArray f = returnFragmentArray(graphDataArray, fragments_fromJSON, colorMap, usedColorIndexes);
		JSONArray transitionInfo = new JSONArray();
		transitionInfo = returnedTransitionArray(this.nash, vms_fromJSON, old_vms_fromJSON, fragments_fromJSON);
		old_vms_fromJSON = vms_fromJSON;
		JSONObject seriesUnit = new JSONObject();
		seriesUnit.put("tuples", json.get("graphData"));
		seriesUnit.put("fragments", f);
		seriesUnit.put("vms", vms_object);
		seriesUnit.put("transition", transitionInfo);
		seriesArray.add(seriesUnit);
		JSONObject returnedJSON = new JSONObject();
		returnedJSON.put("maxFragmentsInVms", MAX_FRAGS_IN_VM);
		returnedJSON.put("series", seriesArray);
		
		File frontendJSON = new File(newFileName);
		try (PrintWriter out = new PrintWriter(frontendJSON)) {
		    out.println(returnedJSON.toJSONString());
		    out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("file could not be created");
		}
	}
	
	
	
	
	public void addQuery(int start, int stop, int value) {
		this.nash.noteQuery(start, stop, value);
	}
	
	
	public static GraphDataObj[] returnGraphDataArray(JSONArray graphData_fromJSON) {
		GraphDataObj[] graphDataArray = new GraphDataObj[graphData_fromJSON.size()];
		for (int i = 0; i < graphData_fromJSON.size(); i++) {
		    // obtaining the i-th result
		    JSONObject result = (JSONObject) graphData_fromJSON.get(i);
		    Double value = ((Long) result.get("value")).doubleValue();
		    int end = ((Long) result.get("end")).intValue();
		    graphDataArray[i] = new GraphDataObj(end, value);
		}
		return graphDataArray;
	}
	public static JSONArray returnFragmentArray(GraphDataObj[] graphDataArray, JSONArray fragments_fromJSON, TreeMap<Integer, Integer> colorMap, TreeSet<Integer> usedColorIndexes) {
		JSONArray f = new JSONArray();
		for (int n = 0; n < fragments_fromJSON.size(); n++) {
			JSONObject result = (JSONObject) fragments_fromJSON.get(n);
			double sum = 0;
			int start = ((Long) result.get("start")).intValue();
			int end = ((Long) result.get("end")).intValue();
			
			for (int j = start; j <= end; j++) {
				int graphDataArrayIndex = 0;
				boolean found = false;
				while (!found) {
					if (graphDataArray[graphDataArrayIndex].end() < (j-1)) {
						graphDataArrayIndex++;
						if (graphDataArrayIndex <= graphDataArray.length) {
							sum+= graphDataArray[graphDataArray.length-1].value();
							found = true;
						}
					} else {
//						System.out.println("sum" + graphDataArray[graphDataArrayIndex].value());
						sum += graphDataArray[graphDataArrayIndex].value();
						found = true;
					}
				}
			}
			double mean = sum / (end - start);
			String color;
			if (!colorMap.containsKey(start)) {
				int colorIndex = r.nextInt(colorArray.length);
				while (usedColorIndexes.contains(colorIndex)) {
					colorIndex = r.nextInt(colorArray.length);
				}
				colorMap.put(start, colorIndex);
				usedColorIndexes.add(colorIndex);
				color = colorArray[colorIndex];
			} else {
				color = colorArray[colorMap.get(start)];
			}
			JSONObject temp = new JSONObject();
			temp.put("start", start);
			temp.put("end", end);
			temp.put("mean", String.format("%.2f",mean));
			temp.put("color" , color);
			f.add(temp);
		}
		return f;
	}
	public static JSONArray assignFragmentsToVMS(JSONArray fragments_fromJSON, JSONArray vms_fromJSON, TreeMap<Integer, Integer> colorMap, TreeSet<Integer> usedColorIndexes, GraphDataObj[] graphDataArray){
		JSONArray vms_object = new JSONArray();
		for (int i = 0; i < vms_fromJSON.size(); i++) {
			JSONObject result = (JSONObject) vms_fromJSON.get(i);
			JSONArray vms_fragments_fromJSON = (JSONArray) result.get("fragments");
			JSONObject individualVM = new JSONObject();
			individualVM.put("id", i);
			int fragmentCountPerVM = 0;
			int vms_fragment_popularity = 0;
			JSONArray outputtedFragments = new JSONArray();
			for (int j = 0; j < vms_fragments_fromJSON.size(); j++) {
				JSONObject f = ((JSONObject) fragments_fromJSON.get(j));
				int start = ((Long) f.get("start")).intValue();
				int end = ((Long) f.get("end")).intValue();
				TreeSet<Double> foundValues = new TreeSet<Double>();
				for (int h = start; h <= end; h++) {
					int graphDataArrayIndex = 0;
					boolean found = false;
					while (!found) {
						if (graphDataArray[graphDataArrayIndex].end() < (j-1)) {
							graphDataArrayIndex++;
						} else {
							if (!foundValues.contains(graphDataArray[graphDataArrayIndex].value())){
	//							System.out.println("sum" + graphDataArray[graphDataArrayIndex].value());
								vms_fragment_popularity += graphDataArray[graphDataArrayIndex].value();
								foundValues.add(graphDataArray[graphDataArrayIndex].value());
								
							} 
							found = true;
						}
					}
				}
				JSONObject fragmentsPerVM = new JSONObject();
				String range = "(" + start + ", " + end + ")";
				fragmentsPerVM.put("label", range);
				String color;
				if (!colorMap.containsKey(start)) {
					int colorIndex = r.nextInt(colorArray.length);
					while (usedColorIndexes.contains(colorIndex)) {
						colorIndex = r.nextInt(colorArray.length);
					}
					colorMap.put(start, colorIndex);
					usedColorIndexes.add(colorIndex);
					color = colorArray[colorIndex];
				} else {
					color = colorArray[colorMap.get(start)];
				}
				
				
				fragmentsPerVM.put("color" , color);
				fragmentsPerVM.put("width", start-end);
				fragmentsPerVM.put("popularity", vms_fragment_popularity);
				outputtedFragments.add(fragmentsPerVM);
				fragmentCountPerVM++;
			}
			if (fragmentCountPerVM > MAX_FRAGS_IN_VM) MAX_FRAGS_IN_VM = fragmentCountPerVM;
			individualVM.put("fragments", outputtedFragments);
			vms_object.add(individualVM);
		}
		return vms_object;
	}
	
	public static JSONArray returnedTransitionArray(NashDB nash, JSONArray vms_fromJSON, JSONArray old_vms_fromJSON, JSONArray fragments_fromJSON) { 
		JSONArray transition_Array = new JSONArray();
		List<VM> before = computeVMList(old_vms_fromJSON, fragments_fromJSON);
		List<VM> after = computeVMList(vms_fromJSON, fragments_fromJSON);
		Collection<TransitionAction<VM>> plan = nash.computeTransition(before, after);
		for (TransitionAction<VM> ta : plan) {

			JSONObject transition_object = new JSONObject();
			if (ta.isTransition()) 
				transition_object.put("from" , Integer.parseInt(ta.getBefore().toString().replaceAll("[^0-9]", "")));
				transition_object.put("to" , Integer.parseInt(ta.getAfter().toString().replaceAll("[^0-9]", "")));
			if (ta.isDelete()) {
				transition_object.put("from" , Integer.parseInt(ta.getBefore().toString().replaceAll("[^0-9]", "")));
				transition_object.put("to" , -1);
			}
			if (ta.isProvision()) {
				transition_object.put("from" , -1);
				transition_object.put("to" , Integer.parseInt(ta.getAfter().toString().replaceAll("[^0-9]", "")));
			}
			transition_Array.add(transition_object);
		}

		return transition_Array;
	}
	
	public static List<VM> computeVMList(JSONArray vms_fromJSON, JSONArray fragments_fromJSON) {
		List<VM> vmList = new ArrayList<>();
		for (int i = 0; i < vms_fromJSON.size(); i++) {
			JSONObject result = (JSONObject) vms_fromJSON.get(i);
			JSONArray vms_fragments_fromJSON = (JSONArray) result.get("fragments");
			JSONArray fragmentsForVM = new JSONArray();
			for (int j = 0; j < vms_fragments_fromJSON.size(); j++) {
				JSONObject f = ((JSONObject) fragments_fromJSON.get(j));
				int start = ((Long) f.get("start")).intValue();
				int end = ((Long) f.get("end")).intValue();
				fragmentsForVM.add(new Fragment(start,end));
			}
			vmList.add(new VM(i, fragmentsForVM));
		}
		return vmList;
	}
}
 