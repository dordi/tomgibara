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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;

import com.tomgibara.pronto.util.Classes;
import com.tomgibara.pronto.util.Reflect;

public class StuppType {

	private static final Class[] CONS_PARAMS = new Class[] { InvocationHandler.class };

	//key doesn't include key property - this means differing keyProperties will be ignored
	//TODO consider changing this
	private static final WeakHashMap<Class, StuppType> instances = new WeakHashMap<Class, StuppType>();
	
	public static StuppType getInstance(Class<?> clss) {
		//inefficient, a key not based on the proxy class would be more efficient, but tricky to make correct
		final Class<?> proxyClass = Proxy.getProxyClass(clss.getClassLoader(), clss);
		return (StuppType) getInstanceForProxyClass(proxyClass, null, null);
	}
	
	public static StuppType getInstance(Class<?> clss, String keyProperty, Class<?> keyClass) {
		final Class<?> proxyClass = Proxy.getProxyClass(clss.getClassLoader(), clss);
		return (StuppType) getInstanceForProxyClass(proxyClass, keyProperty, keyClass);
	}
	
	public static StuppType getInstance(String keyProperty, Class<?> keyClass, ClassLoader classloader, Class<?>... classes) {
		if (classloader == null) classloader = Thread.currentThread().getContextClassLoader();
		final Class<?> proxyClass = Proxy.getProxyClass(classloader, classes);
		return (StuppType) getInstanceForProxyClass(proxyClass, keyProperty, keyClass);
	}
	
	private static StuppType getInstanceForProxyClass(Class<?> proxyClass, String keyProperty, Class<?> keyClass) {
		if (keyProperty == null) {
			Method method = identifyKeyMethod(proxyClass);
			keyProperty = Reflect.propertyName(method.getName());
			if (Reflect.isSetter(method)) {
				keyClass = method.getParameterTypes()[0];
			} else if (Reflect.isGetter(method)) {
				keyClass = method.getReturnType();
			} else {
				throw new IllegalArgumentException("Method annotated with @StuppKey is not an accessor: " + method.getName());
			}
		} else if (keyClass == null) {
			throw new IllegalArgumentException("Key class must be specified with key property name.");
		}
		synchronized (instances) {
			StuppType type = instances.get(proxyClass);
			if (type == null) {
				type = new StuppType(proxyClass, keyProperty, keyClass);
				instances.put(proxyClass, type);
			}
			return type;
		}
	}
	
	//TODO resolve ambiguity around possible setter/getter signature differences
	private static Method identifyKeyMethod(Class<?> proxyClass) {
		Method keyMethod = null;
		final Class<?>[] interfaces = proxyClass.getInterfaces();
		for (Class<?> i : interfaces) {
			for (Method method : i.getMethods()) {
				if (method.isAnnotationPresent(StuppKey.class)) {
					final String name = method.getName();
					if (keyMethod == null) {
						keyMethod = method;
					} else if (!keyMethod.getName().equals(name)) {
						throw new IllegalArgumentException("Conflicting key names defined by " + method.getName() + " and " + name + " on " + i.getName() + " of " + proxyClass.getName());
					}
				}
			}
		}
		if (keyMethod == null) throw new IllegalArgumentException("No StuppKey: " + Arrays.toString(interfaces));
		return keyMethod;
	}

	private final Class<?> proxyClass;

	final String keyProperty;
	final Class<?> keyClass;
	final HashSet<String> propertyNames;
	final HashMap<Method, String> methodPropertyNames;
	
	private StuppType(Class<?> clss, String keyProperty, Class keyClass) {
		
		//generate method property name map
		HashMap<Method, String> methodPropertyNames = new HashMap<Method, String>();
		final Class<?>[] interfaces = clss.getInterfaces();
		for (Class<?> i : interfaces) {
			for (Method method : i.getMethods()) {
				if (Reflect.isSetter(method) || Reflect.isGetter(method)) {
					final String propertyName = Reflect.propertyName(method.getName());
					methodPropertyNames.put(method, propertyName);
				}
			}
		}
		
		//assign values
		this.keyProperty = keyProperty;
		this.keyClass = keyClass;
		this.proxyClass = clss;
		this.methodPropertyNames = methodPropertyNames;
		this.propertyNames = new HashSet<String>(methodPropertyNames.values());
		
		if (!propertyNames.contains(keyProperty)) throw new IllegalArgumentException("No method for key property: " + keyProperty);
	}

	public boolean instanceImplements(Class<?> clss) {
		return clss.isAssignableFrom(proxyClass);
	}
	
	public boolean keyAssignableFrom(Class<?> clss) {
		return keyClass.isAssignableFrom(clss);
	}

	public Object newInstance() {
		try {
			return proxyClass.getConstructor(CONS_PARAMS).newInstance(new Object[] { new StuppHandler(this) });
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		for (Class<?> clss : proxyClass.getInterfaces()) {
			if (sb.length() > 1) sb.append(", ");
			sb.append(clss.getName());
		}
		sb.append('>');
		return sb.toString();
	}

	void checkKey(Object key) {
		if (keyClass.isPrimitive()) {
			if (key == null) throw new IllegalArgumentException("Primitive key cannot be null");
			if (key.getClass() != Classes.classForPrimitive(keyClass)) throw new IllegalArgumentException("Key class must be " + keyClass +" not " + key.getClass());
		} else {
			if (key != null && !keyAssignableFrom(key.getClass())) throw new IllegalArgumentException("Invalid key class: " + key.getClass());
		}
	}
	
}
