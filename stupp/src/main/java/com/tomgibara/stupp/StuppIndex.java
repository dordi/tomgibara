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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.tomgibara.stupp.ann.StuppIndexDefinition;

//TODO should throw meaningful exception when not in scope
public abstract class StuppIndex<C> {

	// statics
	
	private static final Pattern validNamePattern = Pattern.compile("[A-Za-z]+");
	
	private static final HashMap<Class<? extends Annotation>, Constructor<? extends StuppIndex<?>>> indexCons = new HashMap<Class<? extends Annotation>, Constructor<? extends StuppIndex<?>>>();
	
	static void checkName(String name) {
		if (!validNamePattern.matcher(name).matches()) throw new IllegalArgumentException("Index name " + name + "does not match pattern: " + validNamePattern.pattern());
	}
	
	//this can probably avoid being exposed unless classloader situations arise
	//note: due to the large number of checks peformed by this method, callers should first confirm that the annotationType has not already been registered
	static void registerIndexAnnotation(Class<? extends Annotation> annotationType) {
		//null check
		if (annotationType == null) throw new IllegalArgumentException("null annotationType");
		final StuppIndexDefinition definition = annotationType.getAnnotation(StuppIndexDefinition.class);
		//no index class defined
		if (definition == null) throw new IllegalArgumentException("No StuppIndexDefinition for annotation: " + annotationType);
		//does the annotation have a name method?
		try {
			Method method = annotationType.getMethod("name", Stupp.NO_PARAMS);
			if (method.getReturnType() != String.class) throw new IllegalArgumentException("Annotation has name property which is not a String: " + method.getReturnType());
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Annotation has no name property.");
		}
		final Class<? extends StuppIndex<?>> indexClass = definition.value();
		if (indexClass == null) throw new IllegalArgumentException("null indexClass");
		//can't trust generics - is it an index?
		if (!StuppIndex.class.isAssignableFrom(indexClass)) throw new IllegalArgumentException("Index class does not extend StuppIndex: " + indexClass);
		//is it concrete?
		if (Modifier.isAbstract(indexClass.getModifiers())) throw new IllegalArgumentException("Index class is abstract: " + indexClass);
		//does it have a relevant constructor?
		final Constructor<? extends StuppIndex<?>> constructor;
		try {
			constructor = indexClass.getConstructor(StuppProperties.class, annotationType);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Index has no (StuppProperties, Annotation) constructor: " + indexClass);
		}
		//all checks done, now register
		synchronized (indexCons) {
			// no need to check for duplication, call is idempotent
			indexCons.put(annotationType, constructor);
		}
	}
	
	static String checkForIndexAnnotation(Annotation annotation) {
		final Class<? extends Annotation> annotationType = annotation.annotationType();
		for (Annotation ann : annotationType.getAnnotations()) {
			if (ann.annotationType() == StuppIndexDefinition.class) {
				final boolean alreadyRegistered;
				synchronized(indexCons) {
					alreadyRegistered = indexCons.containsKey(annotationType);
				}
				if (!alreadyRegistered) registerIndexAnnotation(annotationType);
				return Stupp.nameOfAnnotation(annotation);
			}
		}
		return null;
	}

	static StuppIndex<?> createIndex(StuppProperties properties, Annotation definition) {
		synchronized (indexCons) {
			Constructor<? extends StuppIndex<?>> constructor = indexCons.get(definition.annotationType());
			if (constructor == null) throw new IllegalStateException("No constructor for index definition annotation: " + definition);
			try {
				return constructor.newInstance(properties, definition);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	static Collection<? extends StuppIndex<?>> createIndices(HashMap<String, StuppProperties> indexProperties, HashMap<String, Annotation> indexDefinitions) {
		if (indexProperties.isEmpty()) return Collections.emptySet();
		ArrayList<StuppIndex<?>> list = new ArrayList<StuppIndex<?>>(indexProperties.size());
		for (Map.Entry<String, StuppProperties> entry : indexProperties.entrySet()) {
			final String name = entry.getKey();
			final Annotation definition = indexDefinitions.get(name);
			final StuppIndex<?> index;
			if (definition == null) {
				//default index
				//TODO this could be controlled via some form of policy
				index = new StuppUniqueIndex(entry.getValue(), name, true);
			} else {
				index = createIndex(entry.getValue(), definition);
			}
			list.add(index);
		}
		return list;
	}
	
	// fields
	
	final StuppProperties properties;
	final String name;

	StuppScope scope = null;

	public StuppIndex(StuppProperties properties, String name) {
		if (getClass() != StuppGlobalIndex.class) checkName(name);
		this.properties = properties;
		this.name = name;
	}

	// accessors
	
	public StuppProperties getProperties() {
		return properties;
	}
	
	public String getName() {
		return name;
	}

	public abstract Class<C> getCriteriaClass();

	// public methods (require scope to be set and must take lock)

	public abstract Collection<Object> get(C criteria);

	public abstract Object getSingle(C criteria);

	public abstract boolean containsObject(Object object);

	//override for efficiency
	public Collection<Object> getAll() {
		final ArrayList<Object> list = new ArrayList<Object>();
		final StuppLock lock = scope.lock;
		lock.lock();
		try {
			for (Object object : all()) list.add(object);
		} finally {
			lock.unlock();
		}
		return list;
	}

	//override for efficiency
	public boolean matches(C criteria) {
		return getSingle(criteria) != null;
	}
	
	// package methods

	// move methods onto a StuppCollator class
	
	//assumes object is valid stupp object
	StuppTuple getValue(Object object) {
		return getValue(Stupp.getHandlerFast(object).getProperties());
	}
	
	StuppTuple getValue(HashMap<String, Object> values) {
		return getValue(values, null, null);
	}
	
	StuppTuple getValue(HashMap<String, Object> values, String propertyName, Object value) {
		final String[] propertyNames = properties.propertyNames;
		final int length = propertyNames.length;
		final Object[] arr = new Object[length];
		for (int i = 0; i < length; i++) {
			final String property = propertyNames[i];
			arr[i] = propertyName != null && property.equals(propertyName) ? value : values.get(property);
		}
		return properties.combine(arr, false, false);
	}
	
	//assumed to be from client code - checks types
	StuppTuple getValue(Object[] arr, boolean checkTypes) {
		return properties.combine(arr, checkTypes, false);
	}

	//returns an iterator over all instances, should not make a copy
	abstract Iterable<Object> all();

	//TODO stopgap until we have some degree of support for transactions
	abstract void checkUpdate(Object object, StuppTuple oldValue, StuppTuple newValue) throws IllegalArgumentException;

	abstract void performUpdate(Object object, StuppTuple oldValue, StuppTuple newValue);

	abstract void reset();
}