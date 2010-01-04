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
import java.util.LinkedHashSet;
import java.util.WeakHashMap;

import com.tomgibara.pronto.util.Classes;
import com.tomgibara.pronto.util.Reflect;

public class StuppType {

	private static final Class<?>[] CONS_PARAMS = new Class<?>[] { InvocationHandler.class };

	//key doesn't include key property - this means differing keyProperties will be ignored
	//TODO consider changing this
	private static final WeakHashMap<Class<?>, StuppType> instances = new WeakHashMap<Class<?>, StuppType>();
	
	private static ClassLoader nonNullClassLoader(ClassLoader classLoader, Class<?> clss) {
		if (classLoader != null) return classLoader;
		classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader != null) return classLoader;
		if (classLoader == null) classLoader = clss.getClassLoader();
		return classLoader;
	}
	
	//TODO rename static methods
	
	public static Definition newDefinition(Class<?> clss) {
		return newDefinition(null, clss);
	}
	
	public static Definition newDefinition(Class<?>... classes) {
		return newDefinition(null, classes);
		
	}
	
	public static Definition newDefinition(ClassLoader classLoader, Class<?> clss) {
		classLoader = nonNullClassLoader(classLoader, clss);
		final Class<?> proxyClass = Proxy.getProxyClass(classLoader, clss);
		return new Definition(proxyClass);
	}
	
	public static Definition newDefinition(ClassLoader classLoader, Class<?>... classes) {
		//TODO unpleasant change of behaviour here
		classLoader = nonNullClassLoader(classLoader, StuppType.class);
		final Class<?> proxyClass = Proxy.getProxyClass(classLoader, classes);
		return new Definition(proxyClass);
	}

	//convenience method
	public static StuppType getInstance(Class<?> clss) {
		return newDefinition(clss).getType();
	}
	
	private static StuppType getInstance(Definition def) {
		Class<?> proxyClass = def.proxyClass;
		synchronized (instances) {
			StuppType type = instances.get(proxyClass);
			if (type == null) {
				type = new StuppType(def);
				instances.put(proxyClass, type);
			}
			return type;
		}
	}
	
	private final Class<?> proxyClass;

	final HashSet<String> propertyNames;
	final HashMap<Method, String> methodPropertyNames;
	final HashMap<String, Class<?>> propertyClasses;
	final StuppProperties keyProperties;
	final StuppProperties equalityProperties;
	
	private StuppType(Definition def) {
		proxyClass = def.proxyClass;
		methodPropertyNames = def.methodPropertyNames;
		propertyClasses = def.propertyClasses;
		propertyNames = new HashSet<String>(methodPropertyNames.values());
		keyProperties = new StuppProperties(this, def.keyProperties);
		equalityProperties = new StuppProperties(this, def.equalityProperties);
	}

	public boolean instanceImplements(Class<?> clss) {
		return clss.isAssignableFrom(proxyClass);
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

	//XXX
//	void checkKey(Object key) {
//		if (keyClass.isPrimitive()) {
//			if (key == null) throw new IllegalArgumentException("Primitive key cannot be null");
//			if (key.getClass() != Classes.classForPrimitive(keyClass)) throw new IllegalArgumentException("Key class must be " + keyClass +" not " + key.getClass());
//		} else {
//			if (key != null && !keyAssignableFrom(key.getClass())) throw new IllegalArgumentException("Invalid key class: " + key.getClass());
//		}
//	}
	
	StuppKeyedIndex createPrimaryIndex() {
		return new StuppUniqueIndex(keyProperties, true);
	}
	
	Collection<? extends StuppIndex<?>> createSecondaryIndices() {
		//TODO support more general type annotations in future?
		//TODO could cache properties object
		return Collections.emptySet();
	}
	
	// inner classes
	
	public static class Definition {
		
		final Class<?> proxyClass;
		final HashMap<Method, String> methodPropertyNames;
		final HashMap<String, Class<?>> propertyClasses;
		String[] keyProperties = null;
		String[] equalityProperties = null;
		
		private Definition(Class<?> proxyClass) {
			//generate method property name map and type map
			HashMap<Method, String> methodPropertyNames = new HashMap<Method, String>();
			HashMap<String, Class<?>> propertyClasses = new HashMap<String, Class<?>>();
			final Class<?>[] interfaces = proxyClass.getInterfaces();
			for (Class<?> i : interfaces) {
				for (Method method : i.getMethods()) {
					final boolean setter = Reflect.isSetter(method);
					final boolean getter = Reflect.isGetter(method);
					if (setter || getter) {
						final String propertyName = Reflect.propertyName(method.getName());
						methodPropertyNames.put(method, propertyName);
						final Class<?> c = setter ? method.getParameterTypes()[0] : method.getReturnType();
						final Class<?> k = propertyClasses.get(propertyName);
						if (c != k) {
							if (k == null) {
								propertyClasses.put(propertyName, c);
							} else {
								boolean cek = k.isAssignableFrom(c);
								boolean kec = c.isAssignableFrom(k);
								if (!cek && !kec) {
									throw new IllegalArgumentException("Incompatible setter/getter types: " + propertyName);
								} else if (getter && cek || setter && kec) {
									throw new IllegalArgumentException("Incompatible setter type too general: " + propertyName);
								} else if (getter) {
									propertyClasses.put(propertyName, c);
								}
							}
						}
					}
				}
			}
			
			//assign values
			this.proxyClass = proxyClass;
			this.methodPropertyNames = methodPropertyNames;
			this.propertyClasses = propertyClasses;
			
			//default other state based on annotations
			processAnnotations();
		}
		
		public Definition setKeyProperties(String... keyProperties) {
			if (keyProperties == null) throw new IllegalArgumentException("null keyProperties");
			
			final int length = keyProperties.length;
			final HashSet<String> set = new HashSet<String>();
			//TODO doesn't actually check if there is a corresponding setter - does that matter?
			for (int i = 0; i < length; i++) {
				final String property = keyProperties[i];
				if (property == null) throw new IllegalArgumentException("Null key property at index " + i);
				Class<?> clss = propertyClasses.get(property);
				if (clss == null) throw new IllegalArgumentException("Unknown property " + property + " at index " + i);
				if (!set.add(property)) throw new IllegalArgumentException("Duplicate key property " + property + " at index " + i);
			}

			this.keyProperties = keyProperties.clone();
			return this;
		}
		
		public Definition setEqualityProperties(String... equalityProperties) {
			if (equalityProperties == null) throw new IllegalArgumentException("null equalityProperties");
			
			final int length = equalityProperties.length;
			final LinkedHashSet<String> set = new LinkedHashSet<String>();
			//TODO doesn't actually check if there is a corresponding setter - does that matter?
			for (int i = 0; i < length; i++) {
				final String property = equalityProperties[i];
				if (property == null) throw new IllegalArgumentException("Null equality property at index " + i);
				if (!propertyClasses.containsKey(property)) throw new IllegalArgumentException("Unknown property " + property + " at index " + i);
				if (!set.add(property)) throw new IllegalArgumentException("Duplicate equality property " + property + " at index " + i);
				
			}

			this.equalityProperties = (String[]) set.toArray(new String[set.size()]);
			return this;
		}
		
		private void checkComplete() {
			//TODO relax this constraint in future - allow there to be no primary key
			//missing keyProperties
			if (keyProperties.length == 0) throw new IllegalStateException("No primary key defined");
		}

		public StuppType getType() {
			checkComplete();
			return getInstance(this);
		}
		
		private void processAnnotations() {
			ArrayList<Method> keyMethods = new ArrayList<Method>();
			ArrayList<Method> equalityMethods = new ArrayList<Method>();
			final Class<?>[] interfaces = proxyClass.getInterfaces();
			for (Class<?> i : interfaces) {
				for (Method method : i.getMethods()) {
					if (!Reflect.isSetter(method)) continue;
					final StuppKey key = method.getAnnotation(StuppKey.class);
					final StuppEquality equality = method.getAnnotation(StuppEquality.class);
					if (key != null) {
						int index = key.index();
						final int size = keyMethods.size();
						if (index < 0 || index == size) {
							keyMethods.add(method);
						} else if (index < size) {
							keyMethods.set(index, method);
						} else {
							while (index > size) {
								keyMethods.add(null);
								index --;
							}
							keyMethods.add(method);
						}
					}
					if (equality != null || key != null) {
						equalityMethods.add(method);
					}
				}
			}
			//ensure key properties have no gaps
			while (keyMethods.remove(null));
			//create key arrays
			{
				final int length = keyMethods.size();
				final String[] keyProperties = new String[length];
				final Class<?>[] keyClasses = new Class<?>[length];
				for (int i = 0; i < length; i++) {
					Method method = keyMethods.get(i);
					keyProperties[i] = Reflect.propertyName(method.getName());
					keyClasses[i] = method.getParameterTypes()[0];
				}
				this.keyProperties = keyProperties;
			}
			//create equality array
			{
				final int length = equalityMethods.size();
				final String[] equalityProperties = new String[length];
				for (int i = 0; i < length; i++) {
					Method method = equalityMethods.get(i);
					equalityProperties[i] = Reflect.propertyName(method.getName());
				}
				this.equalityProperties = equalityProperties;
			}
		}
		
	}
	
}
