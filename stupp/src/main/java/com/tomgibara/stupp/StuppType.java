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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
		return (StuppType) getInstanceForProxyClass(proxyClass, null, null, null);
	}
	
	public static StuppType getInstance(Class<?> clss, String keyProperty, Class<?> keyClass, String... equalityProperties) {
		final Class<?> proxyClass = Proxy.getProxyClass(clss.getClassLoader(), clss);
		return (StuppType) getInstanceForProxyClass(proxyClass, keyProperty, keyClass, equalityProperties);
	}
	
	public static StuppType getInstance(String keyProperty, Class<?> keyClass, String[] equalityProperties, ClassLoader classloader, Class<?>... classes) {
		if (classloader == null) classloader = Thread.currentThread().getContextClassLoader();
		final Class<?> proxyClass = Proxy.getProxyClass(classloader, classes);
		return (StuppType) getInstanceForProxyClass(proxyClass, keyProperty, keyClass, equalityProperties);
	}
	
	private static StuppType getInstanceForProxyClass(Class<?> proxyClass, String keyProperty, Class<?> keyClass, String[] equalityProperties) {
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
		
		if (equalityProperties == null || equalityProperties.length == 0) {
			Method[] methods = identifyEqualityMethods(proxyClass);
			if (methods.length == 0) {
				equalityProperties = new String[] { keyProperty };
			} else {
				equalityProperties = new String[methods.length];
				for (int i = 0; i < methods.length; i++) {
					equalityProperties[i] = Reflect.propertyName(methods[i].getName());
				}
			}
		}
		synchronized (instances) {
			StuppType type = instances.get(proxyClass);
			if (type == null) {
				type = new StuppType(proxyClass, keyProperty, keyClass, equalityProperties);
				instances.put(proxyClass, type);
			}
			return type;
		}
	}
	
	private static Method identifyKeyMethod(Class<?> proxyClass) {
		Method keyMethod = null;
		final Class<?>[] interfaces = proxyClass.getInterfaces();
		for (Class<?> i : interfaces) {
			for (Method method : i.getMethods()) {
				if (!Reflect.isSetter(method)) continue;
				if (!method.isAnnotationPresent(StuppKey.class)) continue;
				final String name = method.getName();
				if (keyMethod == null) {
					keyMethod = method;
				} else if (!keyMethod.getName().equals(name)) {
					throw new IllegalArgumentException("Conflicting key names defined by " + method.getName() + " and " + name + " on " + i.getName() + " of " + proxyClass.getName());
				}
			}
		}
		if (keyMethod == null) throw new IllegalArgumentException("No StuppKey: " + Arrays.toString(interfaces));
		return keyMethod;
	}

	//TODO should roll this into identifyKeyMethod for efficiency
	private static Method[] identifyEqualityMethods(Class<?> proxyClass) {
		final HashSet<Method> equalityMethods = new HashSet<Method>();
		final Class<?>[] interfaces = proxyClass.getInterfaces();
		for (Class<?> i : interfaces) {
			for (Method method : i.getMethods()) {
				if (!Reflect.isSetter(method)) continue;
				if (!method.isAnnotationPresent(StuppEquality.class)) continue;
				equalityMethods.add(method);
			}
		}
		return (Method[]) equalityMethods.toArray(new Method[equalityMethods.size()]);
	}
	
	private final Class<?> proxyClass;

	final String keyProperty;
	final Class<?> keyClass;
	final String[] equalityProperties;
	final HashSet<String> propertyNames;
	final HashMap<Method, String> methodPropertyNames;
	final HashMap<String, Class<?>> propertyClasses;
	
	private StuppType(Class<?> clss, String keyProperty, Class keyClass, String[] equalityProperties) {
		
		//generate method property name map and type map
		HashMap<Method, String> methodPropertyNames = new HashMap<Method, String>();
		HashMap<String, Class<?>> propertyClasses = new HashMap<String, Class<?>>();
		final Class<?>[] interfaces = clss.getInterfaces();
		for (Class<?> i : interfaces) {
			for (Method method : i.getMethods()) {
				final boolean setter = Reflect.isSetter(method);
				final boolean getter = Reflect.isGetter(method);
				if (setter || getter) {
					final String propertyName = Reflect.propertyName(method.getName());
					methodPropertyNames.put(method, propertyName);
					Class<?> c = setter ? method.getParameterTypes()[0] : method.getReturnType();
					Class<?> k = propertyClasses.get(propertyName);
					if (k == null) {
						propertyClasses.put(propertyName, c);
					} else {
						if (k.isAssignableFrom(c)) {
							propertyClasses.put(propertyName, c);
						} else if (!c.isAssignableFrom(k)) {
							throw new IllegalArgumentException("Incompatible setter/getter types: " + propertyName);
						}
					}
				}
			}
		}
		
		//assign values
		this.keyProperty = keyProperty;
		this.keyClass = keyClass;
		this.equalityProperties = equalityProperties;
		this.proxyClass = clss;
		this.methodPropertyNames = methodPropertyNames;
		this.propertyNames = new HashSet<String>(methodPropertyNames.values());
		this.propertyClasses = propertyClasses;
		
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

	// package methods
	
	void checkKey(Object key) {
		if (keyClass.isPrimitive()) {
			if (key == null) throw new IllegalArgumentException("Primitive key cannot be null");
			if (key.getClass() != Classes.classForPrimitive(keyClass)) throw new IllegalArgumentException("Key class must be " + keyClass +" not " + key.getClass());
		} else {
			if (key != null && !keyAssignableFrom(key.getClass())) throw new IllegalArgumentException("Invalid key class: " + key.getClass());
		}
	}
	
	Collection<? extends StuppIndex<?>> newIndices() {
		//TODO support more general type annotations in future?
		//TODO could cache properties object
		return Collections.singleton(new StuppUniqueIndex(new StuppProperties(this, keyProperty), true));
	}
	
}
