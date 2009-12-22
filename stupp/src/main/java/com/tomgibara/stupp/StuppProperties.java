package com.tomgibara.stupp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.tomgibara.pronto.util.Classes;

public final class StuppProperties {

	// fields
	
	final StuppType type;
	final String[] propertyNames;

	// constructors
	
	public StuppProperties(StuppType type, String... propertyNames) {
		final HashSet<String> checkSet = new HashSet<String>();
		for (int i = 0; i < propertyNames.length; i++) {
			final String propertyName = propertyNames[i];
			if (propertyName == null) throw new IllegalArgumentException("null property name at index " + i);
			if (!checkSet.add(propertyName)) throw new IllegalArgumentException("duplicate property name at index " + i +": " + propertyName);
			if (!type.propertyNames.contains(propertyName)) throw new IllegalArgumentException("no such property at index " + i +": " + propertyName);
		}
		this.type = type;
		this.propertyNames = propertyNames.clone();
	}

	// accessors
	
	public StuppType getType() {
		return type;
	}
	
	public String[] getPropertyNames() {
		return propertyNames.clone();
	}

	// package methods

	//TODO add combine method that takes array but checks types
	
	boolean containsNull(Object value) {
		return ((Tuple) value).hasNull();
	}

	ArrayList<String> getNullProperties(Object value) {
		final Tuple tuple = (Tuple) value;
		final Object[] values = tuple.values;
		final ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) names.add(propertyNames[i]);
		}
		return names;
	}
	
	// private utility methods
	
	//may keep reference to supplied values array
	//never returns null
	Object combine(Object[] values, boolean check) {
		if (check) {
			final String[] names = propertyNames;
			final HashMap<String, Class<?>> propertyClasses = type.propertyClasses;
			if (values == null) throw new IllegalArgumentException("Null values array supplied");
			if (values.length != names.length) throw new IllegalArgumentException("Incorrect number of values, expected " + names.length +" and received " + values.length);
			for (int i = 0; i < names.length; i++) {
				final String name = names[i];
				final Object value = values[i];
				final Class<?> clss = propertyClasses.get(name);
				final boolean primitive = clss.isPrimitive();
				if (value == null) {
					if (primitive) throw new IllegalArgumentException("Null values for primitive at index " + i);
				} else {
					if (primitive) {
						if (value.getClass() != Classes.classForPrimitive(clss)) throw new IllegalArgumentException("Value type " + value.getClass() + " does not match required primitive type at index " + i);
					} else if (!clss.isInstance(value)) {
						throw new IllegalArgumentException("Incorrect values type " + value.getClass() + " at index " + i);
					}
				}
			}
		}
		switch (values.length) {
		case 0 : return null;
		//TODO introduce Single, Pair (poss. Triple) for efficiency
		//case 1 : return new Single(values[0]);
		//case 2 : return new Pair(values[0], values[1]);
		//case 3 : return new Triple(values[0], values[1], values[2]);
		default : return new Tuple(values);
		}
	}
	
	// object methods

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StuppProperties)) return false;
		StuppProperties that = (StuppProperties) obj;
		if (!this.type.equals(that.type)) return false;
		return Arrays.equals(this.propertyNames, that.propertyNames);
	}

	@Override
	public int hashCode() {
		return type.hashCode() ^ Arrays.hashCode(propertyNames);
	}

	@Override
	public String toString() {
		return type + ":" + Arrays.toString(propertyNames);
	}

	// inner classes
	
	private static class Tuple {
		
		final Object[] values;
		
		public Tuple(Object[] values) {
			this.values = values;
		}
		
		boolean hasNull() {
			for (int i = 0; i < values.length; i++) {
				if (values == null) return true;
			}
			return false;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Tuple)) return false;
			Tuple that = (Tuple) obj;
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
	
}
