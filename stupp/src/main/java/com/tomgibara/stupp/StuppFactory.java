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

//convenience class, designed only for single keys
public class StuppFactory<T, K> {

	private final StuppScope scope;
	private final StuppType type;
	private final StuppUniqueIndex index;
	
	private StuppSequence<? extends K> sequence;

	public StuppFactory(StuppType type, StuppScope scope) {
		this(type, scope, null, null);
	}

	public StuppFactory(StuppType type, StuppScope scope, Class<T> instanceClass, Class<K> keyClass) {
		if (type == null) throw new IllegalArgumentException();
		if (scope == null) throw new IllegalArgumentException();
		if (instanceClass != null && !type.instanceImplements(instanceClass)) throw new IllegalArgumentException();
		Class<? extends StuppIndex<?>> indexClass = type.getIndexClass();
		if (indexClass != StuppUniqueIndex.class) throw new IllegalArgumentException("Factory requires a StuppUniqueIndex primary index.");
		final StuppProperties indexProperties = type.getIndexProperties();
		final Class<?>[] propertyClasses = indexProperties.propertyClasses;
		final int length = propertyClasses.length;
		if (length > 1) throw new IllegalArgumentException("Factory requires a single-valued primary index.");
		final Class<?> typeKeyClass = propertyClasses[0];
		if (keyClass != null && !typeKeyClass.isAssignableFrom(keyClass)) throw new IllegalArgumentException("Key class " + typeKeyClass + " cannot be assigned to specified key class " + keyClass);
		scope.addType(type);
		this.scope = scope;
		this.type = type;
		this.index = (StuppUniqueIndex) scope.getPrimaryIndex(type);
	}

	public StuppType getType() {
		return type;
	}
	
	public StuppScope getScope() {
		return scope;
	}
	
	public void setSequence(StuppSequence<? extends K> sequence) {
		this.sequence = sequence;
	}
	
	public StuppSequence<? extends K> getSequence() {
		return sequence;
	}
	
	public T newInstance() {
		if (sequence == null) throw new IllegalStateException("No sequence supplied");
		K key = sequence.next();
		if (key == null) return null;
		return newInstance(key);
	}
	
	public T newInstance(K key) {
		Object object = type.newInstance();
		index.properties.tupleFromValues(key).setOn(object);
		scope.attach(object);
		return (T) object;
	}

	public T getInstance(K key) {
		return (T) index.getSingle(key);
	}

	public Collection<T> getAllInstances() {
		return (Collection<T>) index.getAll();
	}

	public boolean deleteInstance(T instance) {
		return scope.detach(instance);
	}
	
}
