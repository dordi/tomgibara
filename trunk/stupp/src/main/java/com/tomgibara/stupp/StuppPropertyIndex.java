package com.tomgibara.stupp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

public class StuppPropertyIndex extends StuppIndex<StuppPropertyIndex.PropertyCriteria> {

	// statics
	
	public static class PropertyCriteria {

		final Object[] values;

		public PropertyCriteria(Object... values) {
			this.values = values;
		}

	}

	// fields
	
	private final HashMap<Object, Set<Object>> index = new HashMap<Object, Set<Object>>();

	// constructors

	public StuppPropertyIndex(StuppProperties properties) {
		super(properties);
	}
	
	// index methods
	
	@Override
	void checkUpdate(Object object, Object oldValue, Object newValue) throws IllegalArgumentException {
		/* never fails */
	}

	@Override
	void performUpdate(Object object, Object oldValue, Object newValue) {
		if (oldValue != null) {
			final Set<Object> set = index.get(oldValue);
			set.remove(object);
			if (set.isEmpty()) index.remove(oldValue);
		}
		if (newValue != null) {
			Set<Object> set = index.get(newValue);
			if (set == null) {
				set = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
				set = new HashSet<Object>();
				index.put(newValue, set);
			}
			set.add(object);
		}
	}

	@Override
	void reset() {
		index.clear();
	}

	@Override
	public Collection<Object> get(PropertyCriteria criteria) {
		return getForValue(criteria.values);
	}

	@Override
	public Object getSingle(PropertyCriteria criteria) {
		return getSingleForValue(criteria.values);
	}

	// public helper methods
	
	public Collection<Object> getForValue(Object... values) {
		final Object value = getValue(values);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			HashSet<Object> result = new HashSet<Object>();
			Set<Object> set = index.get(value);
			if (set != null) result.addAll(set);
			return result;
		} finally {
			lock.unlock();
		}
	}

	public Object getSingleForValue(Object... values) {
		final Object value = getValue(values);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			Set<Object> set = index.get(value);
			return set == null ? null : set.iterator().next();
		} finally {
			lock.unlock();
		}
	}
}
