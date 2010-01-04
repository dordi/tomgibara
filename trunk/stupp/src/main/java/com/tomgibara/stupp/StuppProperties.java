/*
 * Copyright 2009 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
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
	final Class<?>[] propertyClasses;

	// constructors
	
	public StuppProperties(StuppType type, String... propertyNames) {
		final HashSet<String> checkSet = new HashSet<String>();
		final Class<?>[] propertyClasses = new Class<?>[propertyNames.length];
		final HashMap<String, Class<?>> map = type.propertyClasses;
		for (int i = 0; i < propertyNames.length; i++) {
			final String propertyName = propertyNames[i];
			if (propertyName == null) throw new IllegalArgumentException("null property name at index " + i);
			if (!checkSet.add(propertyName)) throw new IllegalArgumentException("duplicate property name at index " + i +": " + propertyName);
			if (!type.propertyNames.contains(propertyName)) throw new IllegalArgumentException("no such property at index " + i +": " + propertyName);
			propertyClasses[i] = map.get(propertyName);
		}
		this.type = type;
		this.propertyNames = propertyNames.clone();
		this.propertyClasses = propertyClasses;
	}

	// accessors
	
	public StuppType getType() {
		return type;
	}

	public int size() {
		return propertyNames.length;
	}
	
	public String[] getPropertyNames() {
		return propertyNames.clone();
	}

	public Class<?>[] getPropertyClasses() {
		return propertyClasses.clone();
	}
	
	// methods
	
	public StuppTuple tupleFromValues(Object... values) {
		return combine(values, false, true);
	}
	
	//TODO can genericize?
	public StuppTuple tupleFromInstance(Object instance) {
		StuppHandler handler = Stupp.getHandler(instance);
		if (handler.getType() != type) throw new IllegalArgumentException("Supplied instance has different type: " + handler.getType() + ", properties required type: " + type);
		return handler.getProperties(this);
	}
	
	// package methods

	//TODO add combine method that takes array but checks types
	
	boolean containsNull(Object value) {
		return ((StuppTuple) value).containsNull();
	}

	ArrayList<String> getNullProperties(Object value) {
		final StuppTuple tuple = (StuppTuple) value;
		final Object[] values = tuple.values;
		final ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) names.add(propertyNames[i]);
		}
		return names;
	}
	
	// private utility methods
	
	//may keep reference to supplied values array if clone not set
	//never returns null
	StuppTuple combine(Object[] values, boolean check, boolean clone) {
		if (check) {
			final String[] names = propertyNames;
			if (values == null) throw new IllegalArgumentException("Null values array supplied");
			if (values.length != names.length) throw new IllegalArgumentException("Incorrect number of values, expected " + names.length +" and received " + values.length);
			for (int i = 0; i < names.length; i++) {
				final Object value = values[i];
				final Class<?> clss = propertyClasses[i];
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
		case 0 : return new StuppTuple();
		//TODO introduce Single, Pair (poss. Triple) for efficiency
		//case 1 : return new Single(values[0]);
		//case 2 : return new Pair(values[0], values[1]);
		//case 3 : return new Triple(values[0], values[1], values[2]);
		default : return new StuppTuple(clone ? values.clone() : values);
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
	
}
