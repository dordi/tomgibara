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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

public class StuppScope {

	// statics

	private static final HashSet<StuppIndex<?>> NO_INDICES = new HashSet<StuppIndex<?>>();
	
	// fields
	
	private final HashMap<StuppType, HashMap<Object, Object>> instances = new HashMap<StuppType, HashMap<Object,Object>>();
	private final HashSet<StuppIndex<?>> allIndices = new HashSet<StuppIndex<?>>();
	//TODO store index lists using arrays for efficiency
	private final HashMap<StuppType, HashSet<StuppIndex<?>>> indicesByType = new HashMap<StuppType, HashSet<StuppIndex<?>>>();
	private final HashMap<StuppType, HashMap<String, HashSet<StuppIndex<?>>>> typeLookup = new HashMap<StuppType, HashMap<String,HashSet<StuppIndex<?>>>>();
	
	//may also be taken by allIndices
	StuppLock lock;
	
	public StuppScope(StuppLock lock) {
		this.lock = lock;
	}

	public StuppScope() {
		this(StuppLock.NOOP_LOCK);
	}
	
	public StuppLock getLock() {
		return lock == StuppLock.NOOP_LOCK ? null : lock;
	}
	
	public void setLock(StuppLock lock) {
		this.lock = lock == null ? StuppLock.NOOP_LOCK : lock;
	}
	
	public void addIndex(StuppIndex<?> index) {
		final StuppScope indexScope = index.scope;
		if (indexScope == this) throw new IllegalArgumentException("Index already added to the scope");
		if (indexScope != null) throw new IllegalArgumentException("Index already added to other scope: " + indexScope);
		final StuppProperties properties = index.properties;
		final StuppType type = properties.type;
		
		lock.lock();
		try {
			index.reset();
			HashMap<Object, Object> map = instances.get(type);
			if (map != null) {
				for (Object object : map.values()) {
					final Object value = index.getValue(object);
					try {
						index.checkUpdate(object, null, value);
					} catch (IllegalArgumentException e) {
						index.reset();
						throw e;
					}
				}
			}
			HashMap<String, HashSet<StuppIndex<?>>> propertyLookup = typeLookup.get(type);
			if (propertyLookup == null) {
				propertyLookup = new HashMap<String, HashSet<StuppIndex<?>>>();
				typeLookup.put(type, propertyLookup);
			}
			for (String propertyName : properties.propertyNames) {
				HashSet<StuppIndex<?>> indices = propertyLookup.get(propertyName);
				if (indices == null) {
					indices = new HashSet<StuppIndex<?>>();
					propertyLookup.put(propertyName, indices);
				}
				indices.add(index);
			}
			HashSet<StuppIndex<?>> indices = indicesByType.get(type);
			if (indices == null) {
				indices = new HashSet<StuppIndex<?>>();
				indicesByType.put(type, indices);
			}
			indices.add(index);
			this.allIndices.add(index);
			index.scope = this;
		} finally {
			lock.unlock();
		}
	}
		
	public void removeIndex(StuppIndex<?> index) {
		final StuppScope indexScope = index.scope;
		if (indexScope == null) throw new IllegalArgumentException("Index not added to a scope.");
		if (indexScope != this) throw new IllegalArgumentException("Index added to a different scope: " + indexScope);
		final StuppProperties properties = index.properties;
		final StuppType type = properties.type;

		lock.lock();
		try {
			allIndices.remove(index);
			indicesByType.get(type).remove(index);
			HashMap<String, HashSet<StuppIndex<?>>> propertyLookup = typeLookup.get(type);
			for (String propertyName : properties.propertyNames) {
				propertyLookup.get(propertyName).remove(index);
			}
		 } finally {
			 lock.unlock();
		 }
	 }
		
	 public Set<StuppIndex<?>> getAllIndices() {
		 lock.lock();
		 try {
			 return (HashSet<StuppIndex<?>>) allIndices.clone();
		 } finally {
			 lock.unlock();
		 }
	 }
		

	public Object getObject(StuppType type, Object key) {
		return get(type, key);
	}
	
	public boolean containsObject(Object object) {
		final StuppHandler handler = Stupp.getHandler(object);
		final StuppScope scope = handler.getScope();
		if (scope != this) return false;
		final Object key = handler.getKey();
		if (key == null) return false;
		final StuppType type = handler.getType();
		lock.lock();
		try {
			HashMap<Object, Object> objects = instances.get(type);
			if (objects == null) return false;
			return object == objects.get(key);
		} finally {
			lock.unlock();
		}
	}
	
	public boolean containsKey(StuppType type, Object key) {
		lock.lock();
		try {
			HashMap<Object, Object> objects = instances.get(type);
			return objects != null && objects.get(key) != null;
		} finally {
			lock.unlock();
		}
	}
	
	public boolean containsObjectKey(Object object) {
		final StuppHandler handler = Stupp.getHandler(object);
		final StuppScope scope = handler.getScope();
		if (scope != this) return false;
		final Object key = handler.getKey();
		if (key == null) return false;
		final StuppType type = handler.getType();
		lock.lock();
		try {
			HashMap<Object, Object> objects = instances.get(type);
			return objects != null && objects.get(key) != null;
		} finally {
			lock.unlock();
		}
	}
	
	public boolean attach(Object object) {
		StuppHandler handler = Stupp.getHandler(object);
		Object key = handler.getKey();
		if (key == null) throw new IllegalArgumentException("Object has no key");
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope == this) return false;
			if (scope != null) throw new IllegalArgumentException("Object already has scope.");
			add(handler.getType(), key, object);
			handler.setScope(this);
		} finally {
			lock.unlock();
		}
		return true;
	}
	
	public boolean detach(Object object) {
		StuppHandler handler = Stupp.getHandler(object);
		Object key = handler.getKey();
		if (key == null) return false;
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope != this) return false;
			Object previous = remove(handler.getType(), key);
			//shouldn't have allowed a different instance with the same scope and key in the first place
			if (previous != object) throw new IllegalStateException();
			handler.setScope(null);
		} finally {
			lock.unlock();
		}
		return true;
	}

	public Collection<? extends Object> getAllObjects() {
		HashSet<Object> set = new HashSet<Object>();
		lock.lock();
		try {
			for (HashMap<Object, Object> objects : instances.values()) {
				set.addAll(objects.values());
			}
		} finally {
			lock.unlock();
		}
		return set;
	}
	
	public Collection<? extends Object> getAllObjects(StuppType type) {
		lock.lock();
		try {
			HashMap<Object, Object> objects = instances.get(type);
			return objects == null ? new HashSet<Object>() : new HashSet<Object>(objects.values());
		} finally {
			lock.unlock();
		}
	}

	//assumes lock is held
	HashSet<StuppIndex<?>> getIndices(StuppType type, String propertyName) {
		HashMap<String, HashSet<StuppIndex<?>>> propertyLookup = typeLookup.get(type);
		if (propertyLookup == null) return NO_INDICES;
		HashSet<StuppIndex<?>> indices = propertyLookup.get(propertyName);
		if (indices == null) return NO_INDICES;
		return indices;
	}
	
	//assumes lock is held
	HashSet<StuppIndex<?>> getIndices(StuppType type) {
		HashSet<StuppIndex<?>> indices = indicesByType.get(type);
		return indices == null ? NO_INDICES : indices;
	}
	
	//assumes lock is held
	void tryAttach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		Object key = handler.getKey();
		if (key == null) throw new IllegalArgumentException("Cannot attach object without key to scope " + this);

		StuppScope scope = handler.getScope();
		if (scope == this) return;
		add(handler.getType(), key, object);
		handler.setScope(this);
	}
	
	//assumes lock is held
	void tryDetach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		Object key = handler.getKey();
		if (key == null) return;

		StuppScope scope = handler.getScope();
		if (scope != this) return;
		remove(handler.getType(), key);
		handler.setScope(null);
	}

	//TODO confirm that it is thread safe that key change occurs outside of lock
	//consider exposing 'effective lock' and having handler explicitly take lock
	void updateKey(StuppType type, Object oldKey, Object newKey) {
		lock.lock();
		try {
			final Object object = get(type, oldKey);
			if (object == null) throw new IllegalStateException("Scope did not contain object of type " + type + " with key " + oldKey);
			//must to remove after in case add fails
			add(type, newKey, object);
			remove(type, oldKey);
			final String propertyName = type.keyProperty;
			HashSet<StuppIndex<?>> indices = getIndices(type, propertyName);
			//TODO cache new/old values between calls
			if (!indices.isEmpty()) {
				for (StuppIndex<?> index : indices) {
					Object newValue = index.getValue(object);
					Object oldValue = index.getValue(object, propertyName, oldKey);
					index.checkUpdate(object, oldValue, newValue);
				}
				for (StuppIndex<?> index : indices) {
					Object newValue = index.getValue(object);
					Object oldValue = index.getValue(object, propertyName, oldKey);
					index.performUpdate(object, oldValue, newValue);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private Object get(StuppType type, Object key) {
		HashMap<Object, Object> objects = instances.get(type);
		return objects == null ? null : objects.get(key);
	}
	
	private void add(StuppType type, Object key, Object object) {
		HashMap<Object, Object> objects = instances.get(type);
		if (objects == null) {
			objects = new HashMap<Object, Object>();
			instances.put(type, objects);
			objects.put(key, object);
		} else {
			Object previous = objects.get(key);
			if (previous != null && previous != object) throw new IllegalArgumentException("Scope already contains object of type " + type + " with key " + key);
			objects.put(key, object);
		}
		HashSet<StuppIndex<?>> indices = getIndices(type);
		//TODO cache new values between calls
		if (!indices.isEmpty()) {
			for (StuppIndex<?> index : indices) {
				Object newValue = index.getValue(object);
				index.checkUpdate(object, null, newValue);
			}
			for (StuppIndex<?> index : indices) {
				Object newValue = index.getValue(object);
				index.performUpdate(object, null, newValue);
			}
		}
	}

	private Object remove(StuppType type, Object key) {
		HashMap<Object, Object> objects = instances.get(type);
		if (objects == null) return null;
		Object object = objects.remove(key);
		if (objects.isEmpty()) instances.remove(type);
		HashSet<StuppIndex<?>> indices = getIndices(type);
		//TODO cache old values between calls
		if (!indices.isEmpty()) {
			for (StuppIndex<?> index : indices) {
				Object oldValue = index.getValue(object);
				index.checkUpdate(object, oldValue, null);
			}
			for (StuppIndex<?> index : indices) {
				Object oldValue = index.getValue(object);
				index.performUpdate(object, oldValue, null);
			}
		}
		return object;
	}

}
