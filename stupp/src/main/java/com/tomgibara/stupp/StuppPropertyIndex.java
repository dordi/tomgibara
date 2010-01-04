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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class StuppPropertyIndex extends StuppKeyedIndex {

	// fields
	
	private final HashMap<StuppTuple, Set<Object>> index = new HashMap<StuppTuple, Set<Object>>();

	// constructors

	public StuppPropertyIndex(StuppProperties properties) {
		super(properties);
	}
	
	// index methods
	
	@Override
	void checkUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) throws IllegalArgumentException {
		/* never fails */
	}

	@Override
	void performUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) {
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
	Iterable<Object> all() {
		return new Iterable<Object>() {
			@Override
			public Iterator<Object> iterator() {
				return new Iterator<Object>() {
					
					private final Iterator<Set<Object>> outer = index.values().iterator();

					private Iterator<Object> inner = Collections.emptySet().iterator();

					private Object next = advance();
					
					@Override
					public boolean hasNext() {
						return next != null;
					}
					
					@Override
					public Object next() {
						if (next == null) throw new NoSuchElementException();
						Object tmp = next;
						next = advance();
						return tmp;
					}
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					private Object advance() {
						while(!inner.hasNext()) {
							if (!outer.hasNext()) return null;
							inner = outer.next().iterator();
						}
						return inner.next();
					}
				};
			}
		};
	}
	
	// keyed iterator methods
	
	@Override
	public Collection<Object> getForKey(Object... values) {
		final Object value = getValue(values, true);
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

	@Override
	public Object getSingleForKey(Object... values) {
		final StuppTuple tuple = getValue(values, true);
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			Set<Object> set = index.get(tuple);
			return set == null ? null : set.iterator().next();
		} finally {
			lock.unlock();
		}
	}
}
