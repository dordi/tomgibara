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

//convenience class
public class StuppFactory<T, K> {

	private final StuppScope scope;
	private final StuppType type;
	
	private StuppSequence<? extends K> sequence;

	public StuppFactory(StuppType type, StuppScope scope) {
		this(type, scope, null, null);
	}

	public StuppFactory(StuppType type, StuppScope scope, Class<T> instanceClass, Class<K> keyClass) {
		if (type == null) throw new IllegalArgumentException();
		if (scope == null) throw new IllegalArgumentException();
		if (instanceClass != null && !type.instanceImplements(instanceClass)) throw new IllegalArgumentException();
		if (keyClass != null && !type.keyAssignableFrom(keyClass)) throw new IllegalArgumentException();
		this.scope = scope;
		this.type = type;
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
		Stupp.setKey(object, key);
		scope.attach(object);
		return (T) object;
	}

	public T getInstance(K key) {
		return (T) scope.getObject(type, key);
	}

	public Collection<T> getAllInstances() {
		return (Collection<T>) scope.getAllObjects(type);
	}

	public boolean deleteInstance(T instance) {
		return scope.detach(instance);
	}
	
}
