package edu.brandeis.nashdb.examples;
import spark.Spark;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import spark.Request;
import spark.Response;
import spark.Route;

public class SampleServer {
	public Scn2JSONFormattor f;
	public static void main(String[] args) {
		Scn2JSONFormattor f = new Scn2JSONFormattor();;
		//by default, Java spark runs on port 4567
		Spark.staticFiles.location("/public");
		Spark.init();
        Spark.post("/updateGraph", (request, response) -> {
        	response.type("application/json");
        	JSONObject json = new JSONObject();
			try {
				json = (JSONObject) new JSONParser().parse(request.body());
			} catch (ParseException e) {
				// error code 400 for invalid input
				response.status(400);
				return "{\"message\":\"Custom 400, invalid input, request body could not be parsed\"}";
			}
        	String queryVolume = ((JSONArray) json.get("query_volume")).toString();
        	String queryBudget = ((JSONArray) json.get("query_budget")).toString();
        	String jsonResponse = f.generateJSON(queryVolume, queryBudget);
        	if (jsonResponse.equals("ERROR")) {
        		response.status(400);
				return "{\"message\":\"Custom 400, invalid input, array sizes are not equal\"}";
        	} else {
        		response.status(200);
        		return jsonResponse;
        	}
        });
	}
}
