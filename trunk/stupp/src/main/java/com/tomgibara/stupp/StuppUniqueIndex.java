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
	void checkUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) throws IllegalArgumentException {
		if (newValue == null) return; //removals always allowed
		if (newValue.equals(oldValue)) return; //no change in values
		if (notNull && properties.containsNull(newValue)) throw new IllegalArgumentException("Value has null properties: " + properties.getNullProperties(newValue));
		if (index.containsKey(newValue)) throw new IllegalArgumentException("Duplicate values on index: " + newValue);
	}

	@Override
	void performUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) {
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
			return object == null ? Collections.emptySet() : Collections.singleton(object);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Object getSingleForKey(Object... values) {
		final StuppTuple tuple = getValue(values, true);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			return index.get(tuple);
		} finally {
			lock.unlock();
		}
	}
}
