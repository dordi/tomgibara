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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.tomgibara.pronto.util.Classes;
import com.tomgibara.pronto.util.Objects;
import com.tomgibara.pronto.util.Reflect;

public class StuppHandler implements InvocationHandler {

	private static final Object[] NO_VALUES = new Object[0];
	
	private final StuppType type;
	private final HashMap<String, Object> values;
	private StuppScope scope;
	
	public StuppHandler(StuppType type) {
		this.type = type;
		this.values = new HashMap<String, Object>(type.propertyNames.size());
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		final String propertyName = type.methodPropertyNames.get(method);
		if (propertyName == null) {
			final String methodName = method.getName();
			if (methodName.equals("equals") && args != null && args.length == 1) {
				Object obj = args[0];
				if (!Proxy.isProxyClass(obj.getClass())) return false;
				Proxy other = (Proxy) obj;
				return this.equals(Proxy.getInvocationHandler(other));
			} else if (methodName.equals("hashCode") && args == null) {
				return this.hashCode();
			} else if (methodName.equals("toString") && args == null) {
				return this.toString();
			} else {
				throw new UnsupportedOperationException();
			}
		} else {
			if (Reflect.isGetter(method)) {
				return values.get(propertyName);
			} else if (Reflect.isSetter(method)) {
				setProperty(proxy, propertyName, args[0], false);
			} else throw new UnsupportedOperationException();
		}
		return null;
	}

	Object getProperty(String property) {
		return values.get(property);
	}

	HashMap<String, Object> getProperties() {
		return values;
	}
	
	StuppTuple getProperties(StuppProperties properties) {
		//TODO investigate possible performance improvements
		final int length = properties.size();
		if (length == 0) return properties.combine(NO_VALUES, false, false);
		final Object[] key = new Object[length];
		final String[] propertyNames = properties.propertyNames;
		for (int i = 0; i < length; i++) {
			key[i] = values.get(propertyNames[i]);
		}
		return properties.combine(key, false, false);
	}
	
	//TODO should be version that takes index name
	StuppTuple getKey() {
		return getProperties(type.indexProperties.get(StuppIndexed.PRIMARY_INDEX_NAME));
	}

	//TODO must implement more efficiently - but - must avoid cost of boxing unecessarily though?
	//assumes that array lengths have been validated
	void setProperties(final Object proxy, final String[] propertyNames, final Object[] values, final boolean checkType) {
		final int length = propertyNames.length;
		if (checkType && length != values.length) throw new IllegalArgumentException("Number of values (" + values.length + ") does not match number of properties (" + length + ")");
		for (int i = 0; i < length; i++) {
			setProperty(proxy, propertyNames[i], values[i], checkType);
		}
	}
	
	void setProperty(final Object proxy, final String propertyName, final Object value, final boolean checkType) {
		//TODO this is probably unsafe wrt properly synchronizing access to this object, investigate
		if (checkType) {
			Class<?> clss = type.propertyClasses.get(propertyName);
			if (value == null) {
				if (clss.isPrimitive()) throw new IllegalArgumentException("Cannot assign null to primitive typed property: " + propertyName);
			} else {
				if (clss.isPrimitive()) clss = Classes.classForPrimitive(clss);
				if (!clss.isInstance(value)) throw new IllegalArgumentException("Invalid type for property " + propertyName +": " + value.getClass());
			}
		}
		Object previous = values.put(propertyName, value);
		if (previous != value && scope != null) {
			final StuppLock lock = scope.lock;
			lock.lock();
			try {
				//TODO make it so that scope work can be rolled back (necessary if index fails)
				//ensure that managed objects are attached
				if (value instanceof Collection<?>) {
					for (Object object : (Collection<?>) value) {
						scope.tryAttach(object);
					}
				}
				if (value instanceof Map<?,?>) {
					final Map<?,?> map = (Map<?,?>) value;
					for (Object object : map.keySet()) scope.tryAttach(object);
					for (Object object : map.values()) scope.tryAttach(object);
				}
				scope.tryAttach(value);
				//TODO optimize by caching old/new values
				//ensure that index is updated
				HashSet<StuppIndex<?>> indices = scope.getIndices(type, propertyName);
				if (!indices.isEmpty()) {
					for (StuppIndex<?> index : indices) {
						StuppTuple oldValue = index.getValue(values, propertyName, previous);
						StuppTuple newValue = index.getValue(values);
						index.checkUpdate(proxy, oldValue, newValue);
					}
					for (StuppIndex<?> index : indices) {
						StuppTuple oldValue = index.getValue(values, propertyName, previous);
						StuppTuple newValue = index.getValue(values);
						index.performUpdate(proxy, oldValue, newValue);
					}
				}
			} catch (IllegalArgumentException e) {
				values.put(propertyName, previous);
				throw e;
			} finally {
				lock.unlock();
			}
		}
	}
	
	StuppType getType() {
		return type;
	}
	
	StuppScope getScope() {
		return scope;
	}
	
	void setScope(final StuppScope scope) {
		if (scope == this.scope) return;
		if (scope == null) {
			StuppScope oldScope = this.scope;
			this.scope = scope;
			for (Object value : values.values()) {
				oldScope.tryDetach(value);
			}
		} else {
			this.scope = scope;
			for (Object value : values.values()) {
				scope.tryAttach(value);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StuppHandler)) return false;
		StuppHandler that = (StuppHandler) obj;
		if (this.type != that.type) return false;
		if (this.scope != that.scope) return false;
		for (String property : type.equalityProperties.propertyNames) {
			if (Objects.notEqual(this.values.get(property), that.values.get(property))) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int h = type.hashCode() ^ Objects.hashCode(scope);
		for (String property : type.equalityProperties.propertyNames) {
			h ^= Objects.hashCode(values.get(property));
		}
		return h;
	}
	
	@Override
	public String toString() {
		return type.toString() + ':' + values.toString();
	}
	
}
