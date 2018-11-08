package edu.brandeis.nashdb.examples;

public class GraphDataObj {
	public int end;
	public double value;
	public GraphDataObj(int end, double value) {
		this.end = end;
		this.value = value;
	}
	public double value() {
		return this.value;
	}
	public int end() {
		return this.end;
	}
}
