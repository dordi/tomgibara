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
import java.util.HashMap;
import java.util.HashSet;

public class StuppScope {

	//private final HashMap<Object, Object> objects = new HashMap<Object, Object>();
	
	private final HashMap<StuppType, HashMap<Object, Object>> instances = new HashMap<StuppType, HashMap<Object,Object>>();
	
	private StuppLock lock;
	
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

	void tryAttach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		Object key = handler.getKey();
		if (key == null) throw new IllegalArgumentException("Cannot attach object without key to scope " + this);
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope == this) return;
			add(handler.getType(), key, object);
			handler.setScope(this);
		} finally {
			lock.unlock();
		}
	}
	
	void tryDetach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		Object key = handler.getKey();
		if (key == null) return;
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope != this) return;
			remove(handler.getType(), key);
			handler.setScope(null);
		} finally {
			lock.unlock();
		}
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
	}

	private Object remove(StuppType type, Object key) {
		HashMap<Object, Object> objects = instances.get(type);
		if (objects == null) return null;
		Object previous = objects.remove(key);
		if (objects.isEmpty()) instances.remove(type);
		return previous;
	}

}
