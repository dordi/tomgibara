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

public class StuppUniqueIndex extends StuppIndex<StuppTuple> {

	// statics
	
	@Target(ElementType.TYPE)
	@StuppIndexDefinition(StuppUniqueIndex.class)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Definition {

		String name() default StuppType.PRIMARY_INDEX_NAME;
		
		boolean notNull() default true;
		
	}
	
	public static Definition newDefinition(final String name, final boolean notNull) {
		return (Definition) Proxy.newProxyInstance(Stupp.class.getClassLoader(), new Class[] { Definition.class }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				final String methodName = method.getName();
				if (methodName.equals("name")) return name;
				if (methodName.equals("notNull")) return notNull;
				if (methodName.equals("annotationType")) return Definition.class;
				throw new UnsupportedOperationException();
			}
		});
	}
	
	// fields
	
	private final HashMap<Object, Object> index = new HashMap<Object, Object>();
	private final boolean notNull;

	// constructors

	public StuppUniqueIndex(StuppProperties properties, Definition ann) {
		super(properties, ann.name());
		this.notNull = ann.notNull();
	}

	//convenience constructor
	public StuppUniqueIndex(StuppProperties properties, String name, boolean notNull) {
		super(properties, name);
		this.notNull = notNull;
	}
	
	// accessors
	
	public boolean isNotNull() {
		return notNull;
	}

	@Override
	public Class<StuppTuple> getCriteriaClass() {
		return StuppTuple.class;
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
	public Collection<Object> get(StuppTuple criteria) {
		Object object = getSingle(criteria);
		return object == null ? Collections.emptySet() : Collections.singleton(object);
	}

	@Override
	public Object getSingle(StuppTuple criteria) {
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			return index.get(criteria);
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
		if (newValue == null) return; //removals always allowed
		if (newValue.equals(oldValue)) return; //no change in values
		if (notNull && newValue.containsNull()) throw new IllegalArgumentException("Value has null properties: " + newValue.getNullProperties());
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
	
}
