package com.tomgibara.stupp;

import java.util.Collection;
import java.util.HashMap;

public abstract class StuppIndex<C> {

	final StuppProperties properties;

	StuppScope scope = null;

	public StuppIndex(StuppProperties properties) {
		if (properties.propertyNames.length == 0) throw new IllegalArgumentException("no property names");
		this.properties = properties;
	}

	// accessors
	
	public StuppProperties getProperties() {
		return properties;
	}

	// public methods (require scope to be set and must take lock)

	public abstract Collection<Object> get(C criteria);

	public abstract Object getSingle(C criteria);

	// package methods

	// move methods onto a StuppCollator class
	
	//assumes object is valid stupp object
	Object getValue(Object object) {
		return getValue(Stupp.getHandlerFast(object).getProperties());
	}
	
	//assumes object is valid stupp object
	//TODO review when indices have been unified
	Object getValue(Object object, String propertyName, Object value) {
		return getValue(Stupp.getHandlerFast(object).getProperties(), propertyName, value);
	}
	
	Object getValue(HashMap<String, Object> values) {
		return getValue(values, null, null);
	}
	
	Object getValue(HashMap<String, Object> values, String propertyName, Object value) {
		final String[] propertyNames = properties.propertyNames;
		final int length = propertyNames.length;
		final Object[] arr = new Object[length];
		for (int i = 0; i < length; i++) {
			final String property = propertyNames[i];
			arr[i] = propertyName != null && property.equals(propertyName) ? value : values.get(property);
		}
		return properties.combine(arr, false);
	}
	
	//assumed to be from client code - checks types
	Object getValue(Object[] arr) {
		return properties.combine(arr, true);
	}
	
	//TODO stopgap until we have some degree of support for transactions
	abstract void checkUpdate(Object object, Object oldValue, Object newValue) throws IllegalArgumentException;

	abstract void performUpdate(Object object, Object oldValue, Object newValue);

	abstract void reset();

}