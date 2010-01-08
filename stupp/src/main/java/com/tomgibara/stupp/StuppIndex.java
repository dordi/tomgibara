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
import java.util.Collection;
import java.util.HashMap;

//TODO should throw meaningful exception when not in scope
public abstract class StuppIndex<C> {

	final StuppProperties properties;
	final String name;

	StuppScope scope = null;

	public StuppIndex(StuppProperties properties, String name) {
		if (properties.propertyNames.length == 0) throw new IllegalArgumentException("no property names");
		this.properties = properties;
		this.name = name;
	}

	// accessors
	
	public StuppProperties getProperties() {
		return properties;
	}
	
	public String getName() {
		return name;
	}

	// public methods (require scope to be set and must take lock)

	public abstract Collection<Object> get(C criteria);

	public abstract Object getSingle(C criteria);

	public abstract boolean containsObject(Object object);

	//override for efficiency
	public Collection<Object> getAll() {
		final ArrayList<Object> list = new ArrayList<Object>();
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			for (Object object : all()) list.add(object);
		} finally {
			lock.unlock();
		}
		return list;
	}

	//override for efficiency
	public boolean matches(C criteria) {
		return getSingle(criteria) != null;
	}
	
	// package methods

	// move methods onto a StuppCollator class
	
	//assumes object is valid stupp object
	StuppTuple getValue(Object object) {
		return getValue(Stupp.getHandlerFast(object).getProperties());
	}
	
	StuppTuple getValue(HashMap<String, Object> values) {
		return getValue(values, null, null);
	}
	
	StuppTuple getValue(HashMap<String, Object> values, String propertyName, Object value) {
		final String[] propertyNames = properties.propertyNames;
		final int length = propertyNames.length;
		final Object[] arr = new Object[length];
		for (int i = 0; i < length; i++) {
			final String property = propertyNames[i];
			arr[i] = propertyName != null && property.equals(propertyName) ? value : values.get(property);
		}
		return properties.combine(arr, false, false);
	}
	
	//assumed to be from client code - checks types
	StuppTuple getValue(Object[] arr, boolean checkTypes) {
		return properties.combine(arr, checkTypes, false);
	}

	//returns an iterator over all instances, should not make a copy
	abstract Iterable<Object> all();

	//TODO stopgap until we have some degree of support for transactions
	abstract void checkUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) throws IllegalArgumentException;

	abstract void performUpdate(Object object, StuppTuple oldValue, StuppTuple newValue);

	abstract void reset();
}