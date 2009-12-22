package com.tomgibara.stupp;

import java.util.Collection;

public abstract class StuppKeyedIndex extends StuppIndex<StuppKeyedIndex.KeyedCriteria> {

	// statics
	
	public static class KeyedCriteria {

		final Object values;

		public KeyedCriteria(Object... values) {
			this.values = values;
		}

	}

	// constructors

	public StuppKeyedIndex(StuppProperties properties) {
		super(properties);
	}

	// methods

	@Override
	public Collection<Object> get(KeyedCriteria criteria) {
		return getForKey(criteria.values);
	}

	@Override
	public Object getSingle(KeyedCriteria criteria) {
		return getSingleForKey(criteria.values);
	}

	@Override
	public boolean containsObject(Object object) {
		return getSingleForKey( getValue(object) ) == object;
	}
	
	//TODO "assumed safe" implementations of these should be exposed to avoid cost of type checking
	
	public abstract Collection<Object> getForKey(Object... values);

	public abstract Object getSingleForKey(Object... values);
	
	//override for efficency
	boolean containsMatch(Object object) {
		return getSingleForKey( getValue(object) ) != null;
	}
	

}
