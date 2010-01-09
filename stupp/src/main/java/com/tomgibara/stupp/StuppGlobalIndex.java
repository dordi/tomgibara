package com.tomgibara.stupp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

class StuppGlobalIndex extends StuppIndex<Void> {

	// fields
	
	//TODO can we use a more efficient data structure here?
	private final Set<Object> index = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

	// constructors
	
	public StuppGlobalIndex(StuppType type) {
		super(type.properties(), "_global_");
	}

	// index methods
	
	@Override
	public Class<Void> getCriteriaClass() {
		return Void.class;
	}
	
	@Override
	public boolean containsObject(Object object) {
		return index.contains(object);
	}

	@Override
	public Collection<Object> get(Void criteria) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getSingle(Void criteria) {
		throw new UnsupportedOperationException();
	}

	@Override
	void checkUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) {
		// nothing to do
	}

	@Override
	void performUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) {
		if (oldValue == null && newValue == null) return;
		if (oldValue != null && newValue != null) return;
		if (newValue == null) {
			index.remove(object);
		} else {
			index.add(object);
		}
	}

	@Override
	Iterable<Object> all() {
		return new ArrayList<Object>(index);
	}

	@Override
	void reset() {
		index.clear();
	}

	
	
}
