package com.tomgibara.stupp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class StuppUniqueIndex extends StuppKeyedIndex {

	// fields
	
	private final HashMap<Object, Object> index = new HashMap<Object, Object>();
	private final boolean notNull;

	// constructors

	public StuppUniqueIndex(StuppProperties properties, boolean notNull) {
		super(properties);
		this.notNull = notNull;
	}
	
	// accessors
	
	public boolean isNotNull() {
		return notNull;
	}
	
	// index methods

	@Override
	void checkUpdate(Object object, Object oldValue, Object newValue) throws IllegalArgumentException {
		if (newValue == null) return; //removals always allowed
		if (newValue.equals(oldValue)) return; //no change in values
		if (notNull && properties.containsNull(newValue)) throw new IllegalArgumentException("Value has null properties: " + properties.getNullProperties(newValue));
		if (index.containsKey(newValue)) throw new IllegalArgumentException("Duplicate values on index: " + newValue);
	}

	@Override
	void performUpdate(Object object, Object oldValue, Object newValue) {
		if (oldValue != null) index.remove(oldValue);
		if (newValue != null) index.put(newValue, object);
	}

	@Override
	void reset() {
		index.clear();
	}

	@Override
	Iterable<Object> all() {
		return index.values();
	}
	
	// keyed index methods
	
	@Override
	public Collection<Object> getForKey(Object... values) {
		final Object value = getValue(values, true);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			Object object = index.get(value);
			return object == null ? Collections.emptySet() : Collections
					.singleton(object);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Object getSingleForKey(Object... values) {
		final Object value = getValue(values, true);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			return index.get(value);
		} finally {
			lock.unlock();
		}
	}
}
