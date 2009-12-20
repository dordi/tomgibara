package com.tomgibara.stupp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class StuppUniqueIndex extends StuppIndex<StuppUniqueIndex.UniqueCriteria> {

	// statics
	
	public static class UniqueCriteria {

		final Object values;

		public UniqueCriteria(Object... values) {
			this.values = values;
		}

	}

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
	public Collection<Object> get(UniqueCriteria criteria) {
		return getForValue(criteria.values);
	}

	@Override
	public Object getSingle(UniqueCriteria criteria) {
		return getSingleForValue(criteria.values);
	}

	// public helper methods
	
	public Collection<Object> getForValue(Object... values) {
		final Object value = getValue(values);
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

	public Object getSingleForValue(Object... values) {
		final Object value = getValue(values);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			return index.get(value);
		} finally {
			lock.unlock();
		}
	}
}
