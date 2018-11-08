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
 
package edu.brandeis.nashdb.examples;

import java.util.Random;

import edu.brandeis.nashdb.FragmentationStrategy;
import edu.brandeis.nashdb.NashDB;

import org.json.simple.JSONArray; 
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;




/**
 * Generates an example JSON structure from random data
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 */
public class ExampleRandomData {
	// Change these three options to adjust the output:
	
	/**
	 * The number of tuples in the dataset to be generated. Reasonable
	 * values between 100 and 2000 (or much higher, but that will take
	 * some processing time, since this example computes an optimal).
	 */
	private static final int NUM_TUPLES = 100;
	
	/**
	 * How many queries to run before fragmentation (values over 500
	 * will roll off due to the default window size).
	 */
	private static final int NUM_QUERIES = 500;
	
	/**
	 * The number of fragments to create. Reasonable values between
	 * 5 and 30. Note that the default VM size is 200. Sometimes, more
	 * fragments will be created.
	 */
	private static final int NUM_FRAGMENTS = 5;
		
	// random seed is fixed for reproducible results
	private static final Random r = new Random(43);
	
	private static void addRandomQuery(NashDB nash) {
		int v1 = r.nextInt(NUM_TUPLES);
		int v2 = r.nextInt(NUM_TUPLES);
		
		if (v1 == v2) v2++;
		
		int start = Math.min(v1, v2);
		int stop = Math.max(v1, v2);
		
		nash.noteQuery(start, stop, 1);
	}
	private static int clearBit(int num, int i) {
		int mask = (1<<i) - 1;
		System.out.println(mask);
		return num & mask;
	}
	public static void main(String[] args) {
		NashDB nash = NashDB.freshNashDBInstance(NUM_TUPLES);
		
		for (int i = 0; i < NUM_QUERIES; i++)
			addRandomQuery(nash);
		System.out.println(nash.toJSON(FragmentationStrategy.OPTIMAL, NUM_FRAGMENTS));
	}
}
