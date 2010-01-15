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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.pronto.util.Arguments;

//TODO hide register and addIndex methods when definitions are done
//TODO remove unecessary locking when indices and types are immutable
public class StuppScope {

	// statics

	private static final HashSet<StuppIndex<?>> NO_INDICES = new HashSet<StuppIndex<?>>();
	
	public static Definition newDefinition() {
		return new Definition();
	}
	
	// fields
	
	//private final HashMap<StuppType, HashMap<Object, Object>> instances = new HashMap<StuppType, HashMap<Object,Object>>();
	private final HashMap<StuppType, StuppGlobalIndex> globalIndices = new HashMap<StuppType, StuppGlobalIndex>();
	private final HashSet<StuppIndex<?>> allIndices = new HashSet<StuppIndex<?>>();
	//TODO store index lists using arrays for efficiency
	private final HashMap<StuppType, HashMap<String, StuppIndex<?>>> indicesByType = new HashMap<StuppType, HashMap<String, StuppIndex<?>>>();
	private final HashMap<StuppType, HashMap<String, HashSet<StuppIndex<?>>>> typeLookup = new HashMap<StuppType, HashMap<String,HashSet<StuppIndex<?>>>>();
	
	//may also be taken by indices
	final StuppLock lock;
	
	private StuppScope(Definition def) {
		lock = def.lock == null ? StuppLock.NOOP_LOCK : def.lock;
		for (StuppType type : def.types) {
			addType(type);
		}
		for (StuppIndex<?> index : StuppIndex.createIndices(def.indexProperties, def.indexDefinitions)) {
			addIndex(index);
		}
	}
	
	public StuppLock getLock() {
		return lock == StuppLock.NOOP_LOCK ? null : lock;
	}
	
	public Set<StuppType> getRegisteredTypes() {
		lock.lock();
		try {
			return new HashSet<StuppType>( indicesByType.keySet() );
		} finally {
			lock.unlock();
		}
	}
	
	public StuppGlobalIndex getGlobalIndex(StuppType type) {
		StuppGlobalIndex index = globalIndices.get(type);
		if (index == null) throw new IllegalArgumentException("Type not registered with scope: " + type);
		return index;
	}
	
	public StuppIndex<?> getPrimaryIndex(StuppType type) {
		return getIndex(type, StuppType.PRIMARY_INDEX_NAME);
	}
	
	public StuppIndex<?> getIndex(StuppType type, String indexName) {
		lock.lock();
		try {
			final HashMap<String, StuppIndex<?>> indices = indicesByType.get(type);
			if (indices == null) throw new IllegalArgumentException("Type not registered with scope: " + type);
			final StuppIndex<?> index = indices.get(indexName);
			if (index == null) throw new IllegalArgumentException("No index with name " + indexName + " registered for type " + type);
			return index;
		 } finally {
			 lock.unlock();
		 }
	}

	public Set<StuppIndex<?>> getIndices(StuppProperties properties) {
		lock.lock();
		try {
			final StuppType type = properties.type;
			if (!globalIndices.containsKey(type)) throw new IllegalArgumentException("Type not registered with scope: " + type);
			final HashSet<StuppIndex<?>> indices = getIndices(type, properties.propertyNames);
			//TODO is there a way around this inefficiency?
			//yes in the future, when scopes have definitions, the structures can be immutable
			return new HashSet<StuppIndex<?>>(indices);
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

	public boolean attach(Object object) {
		StuppHandler handler = Stupp.getHandler(object);
		StuppTuple key = handler.getKey();
		if (key.containsNull()) throw new IllegalArgumentException("Primary key contains null");
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope == this) return false;
			if (scope != null) throw new IllegalArgumentException("Object already has scope.");
			add(handler.getType(), object);
			handler.setScope(this);
		} finally {
			lock.unlock();
		}
		return true;
	}
	
	public boolean detach(Object object) {
		StuppHandler handler = Stupp.getHandler(object);
		lock.lock();
		try {
			StuppScope scope = handler.getScope();
			if (scope != this) return false;
			remove(handler.getType(), object);
			handler.setScope(null);
		} finally {
			lock.unlock();
		}
		return true;
	}

	//TODO possible to do more efficiently via a specific call through to indices?
	public void detachAll() {
		lock.lock();
		try {
			for (StuppGlobalIndex globalIndex : globalIndices.values()) {
				for (Object object : globalIndex.getAll()) {
					detach(object);
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	public Collection<? extends Object> getAllObjects() {
		final ArrayList<Object> list = new ArrayList<Object>();
		lock.lock();
		try {
			for (StuppIndex<?> primaryIndex : globalIndices.values()) {
				for (Object object : primaryIndex.all()) {
					list.add(object);
				}
			}
		} finally {
			lock.unlock();
		}
		return list;
	}

	//assumes lock is held
	HashSet<StuppIndex<?>> getIndices(StuppType type, String propertyName) {
		final HashMap<String, HashSet<StuppIndex<?>>> propertyLookup = typeLookup.get(type);
		if (propertyLookup == null) return NO_INDICES;
		final HashSet<StuppIndex<?>> indices = propertyLookup.get(propertyName);
		if (indices == null) return NO_INDICES;
		return indices;
	}
	
	//assumes lock is held
	HashSet<StuppIndex<?>> getIndices(StuppType type, String[] propertyNames) {
		if (propertyNames.length == 0) return NO_INDICES;
		if (propertyNames.length == 1) return getIndices(type, propertyNames[0]);
		final HashMap<String, HashSet<StuppIndex<?>>> propertyLookup = typeLookup.get(type);
		if (propertyLookup == null) return NO_INDICES;
		final HashSet<StuppIndex<?>> indices = new HashSet<StuppIndex<?>>();
		for (String propertyName : propertyNames) {
			indices.addAll(propertyLookup.get(propertyName));
		}
		return indices;
	}
	
	//assumes lock is held
	Collection<StuppIndex<?>> getIndices(StuppType type) {
		final HashMap<String, StuppIndex<?>> map = indicesByType.get(type);
		if (map == null) throw new IllegalArgumentException("Type not registered with scope: " + type);
		Collection<StuppIndex<?>> indices = map.values();
		return indices == null ? NO_INDICES : indices;
	}
	
	//assumes lock is held
	void tryAttach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		StuppTuple key = handler.getKey();
		if (key.containsNull()) throw new IllegalArgumentException("Cannot attach object without key to scope " + this);

		StuppScope scope = handler.getScope();
		if (scope == this) return;
		add(handler.getType(), object);
		handler.setScope(this);
	}
	
	//assumes lock is held
	void tryDetach(Object object) {
		if (object == null) return;
		StuppHandler handler = Stupp.getHandlerOrNull(object);
		if (handler == null) return;
		StuppTuple key = handler.getKey();
		if (key.containsNull()) return;

		StuppScope scope = handler.getScope();
		if (scope != this) return;
		remove(handler.getType(), object);
		handler.setScope(null);
	}

	//TODO make private when factory has been fixed
	//must be called before type instances can be attached to this scope
	void addType(StuppType type) {
		lock.lock();
		try {
			if (globalIndices.containsKey(type)) return; // already registered
			indicesByType.put(type, new HashMap<String, StuppIndex<?>>());
			addIndex(new StuppGlobalIndex(type));
			for (StuppIndex<?> index : type.createIndices()) {
				addIndex(index);
			}
		} finally {
			lock.unlock();
		}
	}

	private void addIndex(StuppIndex<?> index) {
		final StuppScope indexScope = index.scope;
		if (indexScope == this) throw new IllegalArgumentException("Index already added to the scope");
		if (indexScope != null) throw new IllegalArgumentException("Index already added to other scope: " + indexScope);
		final StuppType type = index.properties.type;
		
		lock.lock();
		try {
			HashMap<String, StuppIndex<?>> indices = indicesByType.get(type);
			if (indices == null) throw new IllegalArgumentException("Type not registered with scope: " + type);
			if (indices.containsKey(index.name)) throw new IllegalArgumentException("Index with name " + index.name + " already registered for type " + type);
			index.reset();
			addIndexImpl(index);
		} finally {
			lock.unlock();
		}
	}

	//TODO probably remove this method
	private void removeIndex(StuppIndex<?> index) {
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

	// must be called with lock, assumes index does not exist and indicesByType has been populated w/ map
	private void addIndexImpl(StuppIndex<?> index) {
		final StuppProperties properties = index.properties;
		final StuppType type = properties.type;

		if (index instanceof StuppGlobalIndex) {
			globalIndices.put(type, (StuppGlobalIndex) index);
		} else {
			StuppGlobalIndex globalIndex = globalIndices.get(type);
			//TODO could optimize, no need to clone collection here
			for (Object object : globalIndex.all()) {
				final StuppTuple value = index.getValue(object);
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
		HashMap<String, StuppIndex<?>> indices = indicesByType.get(type);
		indices.put(index.name, index);
		this.allIndices.add(index);
		index.scope = this;
		
	}
	
	private void add(StuppType type, Object object) {
		final Collection<StuppIndex<?>> indices = getIndices(type);
		//TODO cache new values between calls
		for (StuppIndex<?> index : indices) {
			StuppTuple newValue = index.getValue(object);
			index.checkUpdate(object, null, newValue);
		}
		for (StuppIndex<?> index : indices) {
			StuppTuple newValue = index.getValue(object);
			index.performUpdate(object, null, newValue);
		}
	}

	private void remove(StuppType type, Object object) {
		final Collection<StuppIndex<?>> indices = getIndices(type);
		//TODO cache old values between calls
		for (StuppIndex<?> index : indices) {
			StuppTuple oldValue = index.getValue(object);
			index.checkUpdate(object, oldValue, null);
		}
		for (StuppIndex<?> index : indices) {
			StuppTuple oldValue = index.getValue(object);
			index.performUpdate(object, oldValue, null);
		}
	}

	// inner classes
	
	//TODO implement object methods and cloning
	//TODO should check consistency: that the types which indices depend on have been explicitly added
	//TODO provide addType method that performs transitive closure
	public static class Definition {

		StuppLock lock = null;
		final HashSet<StuppType> types = new HashSet<StuppType>();
		final HashMap<String, StuppProperties> indexProperties = new HashMap<String, StuppProperties>();
		final HashMap<String, Annotation> indexDefinitions = new HashMap<String, Annotation>();
		
		public Definition setLock(StuppLock lock) {
			this.lock = lock;
			return this;
		}
		
		public Definition addType(StuppType type) {
			Arguments.notNull(type, "type");
			types.add(type);
			return this;
		}
		
		public Definition removeType(StuppType type) {
			Arguments.notNull(type, "type");
			types.remove(type);
			return this;
		}
		
		public Definition addIndex(String indexName, StuppProperties properties) {
			Arguments.notNull(indexName, "indexName");
			Arguments.notNull(properties, "properties");
			indexProperties.put(indexName, properties);
			return this;
		}
		
		public Definition removeIndex(String indexName) {
			Arguments.notNull(indexName, "indexName");
			indexProperties.remove(indexName);
			return this;
		}
		
		public Definition setIndexDefinition(Annotation annotation) {
			final String indexName = StuppIndex.checkForIndexAnnotation(annotation);
			if (indexName == null) throw new IllegalArgumentException("Supplied annotation is not an index definition annotation");
			indexDefinitions.put(indexName, annotation);
			return this;
		}
		
		public Definition clearIndexDefinition(String name) {
			Arguments.notNull(name, "name");
			indexDefinitions.remove(name);
			return this;
		}
		
		public StuppScope createScope() {
			return new StuppScope(this);
		}
		
	}
	
}
