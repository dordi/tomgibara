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
import java.util.Map;

import com.tomgibara.pronto.util.Objects;
import com.tomgibara.pronto.util.Reflect;

public class StuppHandler implements InvocationHandler {

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
				Object value = args[0];
				//key is a special case - if we have scope there's more work to do
				if (propertyName.equals(type.keyProperty)) {
					setKey(value, false);
				} else {
					Object previous = values.put(propertyName, value);
					if (previous != value && scope != null) {
						if (value instanceof Collection) {
							for (Object object : (Collection<?>) value) {
								scope.tryAttach(object);
							}
						}
						if (value instanceof Map) {
							final Map<?,?> map = (Map<?,?>) value;
							for (Object object : map.keySet()) scope.tryAttach(object);
							for (Object object : map.values()) scope.tryAttach(object);
						}
						scope.tryAttach(value);
					}
				}
			} else throw new UnsupportedOperationException();
		}
		return null;
	}

	Object getKey() {
		return values.get(type.keyProperty);
	}
	
	void setKey(Object value, boolean check) {
		//ids cannot be persistent in their own right
		//NOTE: these tests can only catch a narrow subset of mistakes
		if (value instanceof Collection) throw new IllegalArgumentException("Attempt to to supply collective id: " + value);
		if (Stupp.getHandlerOrNull(value) != null) throw new IllegalArgumentException("Attempt to supply persistent id: " + value);
		if (check) type.checkKey(value);
		final String propertyName = type.keyProperty;
		if (scope != null) {
			Object previous = values.get(propertyName);
			if (Objects.notEqual(previous, value)) {
				scope.updateKey(type, previous, value);
			}
		}
		values.put(propertyName, value);
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
		for (String property : type.equalityProperties) {
			if (Objects.notEqual(this.values.get(property), that.values.get(property))) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int h = type.hashCode() ^ scope.hashCode();
		for (String property : type.equalityProperties) {
			h ^= Objects.hashCode(values.get(property));
		}
		return h;
	}
	
	@Override
	public String toString() {
		return type.toString() + ':' + values.toString();
	}
	
}
