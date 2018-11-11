package edu.brandeis.nashdb.examples;
import static spark.Spark.*;

public class SampleServer {
	public static void main(String[] args) {
		port(8080);
		
        post("/updateGraph", (request, response) -> {
        	response.type("application/json");
    
        	String queryVolume = request.queryParams("query_volume");
        	String queryBudget = request.queryParams("query_budget");
        	
        	return Scn2JSONFormattor.generateJSON(queryVolume, queryBudget);
        });
	}
}
