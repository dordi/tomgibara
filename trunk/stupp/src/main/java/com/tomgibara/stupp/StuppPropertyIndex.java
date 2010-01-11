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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.tomgibara.pronto.util.Annotations;

public class StuppPropertyIndex extends StuppIndex<StuppTuple> {

	// statics
	
	@Target(ElementType.TYPE)
	@StuppIndexDefinition(StuppPropertyIndex.class)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Definition {

		String name() default StuppType.PRIMARY_INDEX_NAME;
		
	}
	
	public static Definition newDefinition(final String name) {
		return Annotations.instantiate(Definition.class.getClassLoader(), Definition.class, Collections.singletonMap("name", (Object) name), false);
	}
	
	// fields
	
	private final HashMap<StuppTuple, Set<Object>> index = new HashMap<StuppTuple, Set<Object>>();

	// constructors

	public StuppPropertyIndex(StuppProperties properties, Definition ann) {
		super(properties, ann.name());
	}

	//convenience constructor
	public StuppPropertyIndex(StuppProperties properties, String name) {
		super(properties, name);
	}
	
	// convenience methods
	
	public Collection<Object> get(Object... values) {
		return get(properties.tupleFromValues(values));
	}

	public Object getSingle(Object... values) {
		return getSingle(properties.tupleFromValues(values));
	}

	// index methods

	@Override
	public Class<StuppTuple> getCriteriaClass() {
		return StuppTuple.class;
	}

	@Override
	public Collection<Object> get(StuppTuple criteria) {
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			HashSet<Object> result = new HashSet<Object>();
			Set<Object> set = index.get(criteria);
			if (set != null) result.addAll(set);
			return result;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public Object getSingle(StuppTuple criteria) {
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			Set<Object> set = index.get(criteria);
			return set == null ? null : set.iterator().next();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean containsObject(Object object) {
		return getSingle(properties.tupleFromInstance(object)) == object;
	}
	
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
	
}
