package com.tomgibara.stupp;

import java.util.Arrays;

//TODO should tuple maintain reference to properties that created it?
//TODO change to an interface with optimized implementations
public class StuppTuple {
	
	final Object[] values;
	
	public StuppTuple(Object... values) {
		this.values = values;
	}
	
	public boolean containsNull() {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StuppTuple)) return false;
		StuppTuple that = (StuppTuple) obj;
		return Arrays.equals(this.values, that.values);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}