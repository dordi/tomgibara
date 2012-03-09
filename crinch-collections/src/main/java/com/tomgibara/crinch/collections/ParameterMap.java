/*
 * Copyright 2010 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.collections;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.tomgibara.crinch.hashing.PerfectStringHash;

/**
 * <p>
 * A {@link Map} implementation that maps a fixed set of candidate
 * {@link String} keys to {@link Object} values. For dense maps (where the
 * number of values is a significant proportion of the number of possible keys)
 * this implementation should exhibit performance comparable to a
 * {@link HashMap} but with significantly less memory usage under many
 * conditions.
 * </p>
 * 
 * <p>
 * Unlike regular {@link Map} implementations, there is no default, or copy,
 * constructor. Instead the map must be constructed around a set of possible
 * {@link String} keys. Only the keys supplied to a constructor may be used in
 * the maps it constructs. Re-using constructors (by repeatedly using them to
 * create {@link ParameterMap} instances) minimizes the memory required to store the
 * map. Furthermore, the only map operations that allocate new objects are those
 * methods that required to do so by the {@link Map} interface; in-short, object
 * creation strictly minimized. This makes the class ideal for environments
 * where reducing GC overhead is important.
 * <p>
 * 
 * <p>
 * Attempting to insert, into the map, a key that was not specified as part of
 * its construction will generate an {@link IllegalArgumentException}.
 * <p>
 * 
 * <p>
 * {@link ParameterMap} implementations are not safe for concurrent use (and do not
 * attempt to detect concurrent modifications) so access by multiple threads
 * must be externally synchronized. However, {@link Constructor} instances
 * <em>are</em> safe for concurrent use.
 * </p>
 * 
 * @author Tom Gibara
 * 
 * @param <V>
 *            the type of values stored in the map
 */

public final class ParameterMap<V> implements Map<String, V> {

	// statics
	
	//TODO consider reversing NULL assignment to improve performance (needs confirmation)
	private static final Object NULL = new Object();
	
	/**
	 * Creates a new {@link ParameterMap} constructor.
	 * 
	 * @param <V>
	 *            the type of value to be stored in the map
	 * @param keys
	 *            the keys that are permissible for the maps that will be
	 *            generated
	 * @return an object that can construct {@link ParameterMap} instances.
	 */
	
	public static <V> Constructor<V> constructor(String... keys) {
		return new Constructor<V>(keys.clone());
	}

	/**
	 * Creates a new {@link ParameterMap} constructor.
	 * 
	 * @param <V>
	 *            the type of value to be stored in the map
	 * @param keys
	 *            the keys that are permissible for the maps that will be
	 *            generated
	 * @return an object that can construct {@link ParameterMap} instances.
	 */
	
	public static <V> Constructor<V> constructor(Collection<String> keys) {
		return new Constructor<V>((String[]) keys.toArray(new String[keys.size()]));
	}
	
	// fields
	
	private final Constructor<V> cons;
	private final Object[] values;
	private int size;
	
	private EntrySet entrySet = null;
	private KeySet keySet = null;
	private ValuesCollection valuesCollection = null;
	
	// constructors
	
	private ParameterMap(Constructor<V> cons) {
		this.cons = cons;
		values = new Object[cons.keys.length];
		Arrays.fill(values, NULL);
		size = 0;
	}
	
	//used for efficient copy construction
	private ParameterMap(ParameterMap<V> that) {
		this.cons = that.cons;
		this.values = that.values.clone();
		this.size = that.size;
	}
	
	// accessors

	/**
	 * The constructor of the {@link ParameterMap}.
	 * 
	 * @return the constructor used to construct this {@link ParameterMap}
	 */
	
	public Constructor<V> getConstructor() {
		return cons;
	}
	
	// map methods
	
	@Override
	public void clear() {
		if (size > 0) {
			Arrays.fill(values, NULL);
			size = 0;
		}
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean containsKey(Object key) {
		int i = cons.indexOf(key);
		return i != -1 && values[i] != NULL;
	}

	@Override
	public boolean containsValue(Object value) {
		return findValue(value) != -1;
	}

	@Override
	public V get(Object key) {
		final int i = cons.indexOf(key);
		if (i < 0) return null;
		final Object obj = values[i];
		if (obj == NULL) return null;
		return (V) obj;
	}

	public V put(String key, V value) {
		final int i = cons.indexOf(key);
		if (i < 0) throw new IllegalArgumentException("unsupported key: " + key);
		final Object obj = values[i];
		values[i] = value;
		if (obj == NULL) {
			size++;
			return null;
		} else {
			return (V) obj;
		}
	};
	
	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		if (map.size() > 0) {
			for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
	            put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public V remove(Object key) {
		final int i = cons.indexOf(key);
		if (i < 0) return null;
		final Object obj = values[i];
		if (obj == NULL) return null;
		values[i] = NULL;
		size--;
		return (V) obj;
	}
	
	@Override
	public Set<Map.Entry<String, V>> entrySet() {
		final EntrySet entrySet = this.entrySet;
		return entrySet == null ? this.entrySet = new EntrySet() : entrySet;
	}
	
	@Override
	public Set<String> keySet() {
		final KeySet keySet = this.keySet;
		return keySet == null ? this.keySet = new KeySet() : keySet;
	}
	
	@Override
	public Collection<V> values() {
		final ValuesCollection valuesCollection = this.valuesCollection;
		return valuesCollection == null ? this.valuesCollection = new ValuesCollection() : valuesCollection;
	}

	// object methods
	
	@Override
	public ParameterMap<V> clone() {
		return new ParameterMap<V>(this);
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		final int capacity = cons.capacity;
		final String[] keys = cons.keys;
		final Object[] values = this.values;
		for (int i = 0; i < capacity; i++) {
			final Object value = values[i];
			if (value == NULL) continue;
			if (value == null) {
				h += keys[i].hashCode();
			} else {
				h += keys[i].hashCode() ^ value.hashCode();
			}
		}
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Map)) return false;
		final Map<?,?> that = (Map) obj;
		if (this.size != that.size()) return false;
		final Object[] values = this.values;
		final Constructor<V> cons = this.cons;
		for (Map.Entry entry : that.entrySet()) {
			final Object key = entry.getKey();
			final int i = cons.indexOf(key);
			if (i == -1) return false;
			final Object v = values[i];
			if (v == NULL) return false;
			final Object value = entry.getValue();
			if (value == v) continue;
			if (value == null || v == null) return false;
			if (!v.equals(value)) return false;
		}
		return true;
	}
	
	public String toString() {
		if (size == 0) return "{}";
		StringBuilder sb = new StringBuilder();
		final String[] keys = cons.keys;
		final Object[] values = this.values;
		for (int i = 0; i < keys.length; i++) {
			final Object value = values[i];
			if (value == NULL) continue;
			if (sb.length() == 0) {
				sb.append('{');
			} else {
				sb.append(", ");
			}
			sb.append(keys[i]).append('=').append(value == this ? "(this Map)" : value);
		}
		return sb.append('}').toString();
	};
	
	// private utility methods
	
	private int findValue(Object value) {
		final int capacity = cons.capacity;
		final Object[] values = this.values;
		if (value == null) {
			for (int i = 0; i < capacity; i++) {
				if (values[i] == null) return i;
			}
			return -1;
		} else {
			for (int i = 0; i < capacity; i++) {
				final Object v = values[i];
				if (v != null && v.equals(value)) return i;
			}
			return -1;
		}
	}
	
	// inner classes
	
	private abstract class IteratorBase<T> implements Iterator<T> {
		
		//copy some values for efficiency
		final int capacity = cons.capacity;
		final String[] keys = cons.keys;
		final Object[] values = ParameterMap.this.values;
		//state of the iterator
		private int previousIndex;
		private int nextIndex = -1;
		
		IteratorBase() {
			advance();
		}
		
		@Override
		public boolean hasNext() {
			return nextIndex != capacity;
		}
		
		@Override
		public T next() {
			if (nextIndex == capacity) throw new NoSuchElementException();
			final T t = get(nextIndex);
			advance();
			return t;
		}
		
		@Override
		public void remove() {
			if (previousIndex == -1) throw new IllegalStateException();
			values[previousIndex] = NULL;
			size --;
			previousIndex = -1;
		}
		
		private void advance() {
			previousIndex = nextIndex;
			do {
				nextIndex++;
			} while (nextIndex < capacity && values[nextIndex] == NULL);
		}
		
		abstract T get(int index);
		
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<String, V>> {
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public void clear() {
			ParameterMap.this.clear();
		}
		
		@Override
		public boolean isEmpty() {
			return size == 0;
		}
		
		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			int index = find((Map.Entry<String, V>) o);
			if (index == -1) return false;
			values[index] = NULL;
			size--;
			return true;
		}
		
		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			int index = find((Map.Entry<String, V>) o);
			return index != -1;
		}
		
		@Override
		public Iterator<Map.Entry<String, V>> iterator() {
			return new IteratorBase<Map.Entry<String, V>>() {
				
				@Override
				Map.Entry<String, V> get(int index) {
					return new Entry<V>(keys[index], (V) values[index]);
				}
				
			};
		}
		
		private int find(Map.Entry<String, V> entry) {
			final String key = entry.getKey();
			final int index = cons.indexOf(key);
			if (index == -1) return -1;
			final Object v = values[index];
			if (v == NULL) return -1;
			final Object value = entry.getValue();
			if (value == v) return index;
			if (v == null || value == null) return -1;
			if (v.equals(value)) return -1;
			return index;
		}
		
	}
	
	private class KeySet extends AbstractSet<String> {

		@Override
		public int size() {
			return size;
		}
		
		@Override
		public void clear() {
			ParameterMap.this.clear();
		}
		
		@Override
		public boolean isEmpty() {
			return size == 0;
		}
		
		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			final int i = cons.indexOf(o);
			if (i < 0) return false;
			if (values[i] == NULL) return false;
			values[i] = NULL;
			size--;
			return true;
		}
		
		@Override
		public Iterator<String> iterator() {
			return new IteratorBase<String>() {
				
				@Override
				String get(int index) {
					return keys[index];
				}
				
			};
		}

	}
	
	private class ValuesCollection extends AbstractCollection<V> {
		
		@Override
		public int size() {
			return size;
		}
		
		@Override
		public void clear() {
			ParameterMap.this.clear();
		}

		@Override
		public boolean isEmpty() {
			return size == 0;
		}
		
		@Override
		public boolean contains(Object o) {
			return findValue(o) != -1;
		}
		
		@Override
		public boolean remove(Object o) {
			final int i = findValue(o);
			if (i == -1) return false;
			values[i] = NULL;
			return true;
		}

		@Override
		public Iterator<V> iterator() {
			return new IteratorBase<V>() {
				
				@Override
				V get(int index) {
					return (V) values[index];
				}
				
			};
		}
		
	}
	
	private final static class Entry<V> implements Map.Entry<String, V> {

		private final String key;
		private V value;
		
		Entry(String key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public String getKey() {
			return key;
		}
		
		@Override
		public V getValue() {
			return value;
		}
		
		@Override
		public V setValue(V value) {
			final V previous = this.value;
			this.value = value;
			return previous;
		};

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Map.Entry)) return false;
			final Map.Entry<String, V> that = (Map.Entry<String, V>) obj;
			final String thatKey = that.getKey();
			final V thatValue = that.getValue();
			if (thatKey == null || !this.key.equals(thatKey)) return false;
			if (this.value == thatValue) return true;
			if (this.value == null || thatValue == null) return false;
			return this.value.equals(thatValue);
		}
		
		@Override
		public int hashCode() {
			return value == null ? key.hashCode() : key.hashCode() ^ value.hashCode();
		}
		
		@Override
		public String toString() {
			return key + "=" + value;
		}
		
	}
	
	/**
	 * <p>
	 * Instances of this class construct new {@link ParameterMap} objects. They take
	 * the place of the {@link Map} constructors on regular implementations.
	 * </p>
	 * 
	 * <p>
	 * All the methods on this class are safe for concurrent use.
	 * </p>
	 * 
	 * @param <V>
	 *            the type of value to be stored in the maps the constructor
	 *            creates
	 */
	
	public static class Constructor<V> {

		private final int capacity;
		private final String[] keys;
		//performs the perfect hashing
		private final PerfectStringHash mph;
		//lazily instantiated
		private volatile Set<String> keySet = null;

		private Constructor(final String[] keys) {
			this.mph = new PerfectStringHash(keys);
			this.keys = keys;
			this.capacity = keys.length;
		}

		/**
		 * Constructs a new empty {@link ParameterMap}.
		 * 
		 * @return an empty map
		 */
		
		public ParameterMap<V> newMap() {
			return new ParameterMap<V>(this);
		}

		/**
		 * Constructs a new {@link ParameterMap} that contains all the entries in
		 * the supplied Map
		 * 
		 * @param map
		 *            a map to be copied
		 * @return a copy of the supplied {@link Map}
		 * @throws IllegalArgumentException
		 *             if the supplied map contains an invalid key
		 */
		
		public ParameterMap<V> newMap(Map<String, ? extends V> map) throws IllegalArgumentException {
			if (map instanceof ParameterMap) {
				ParameterMap<? extends V> pm = (ParameterMap<? extends V>) map;
				if (pm.cons == this) return new ParameterMap(pm);
				ParameterMap<V> m = new ParameterMap<V>(this);
				final String[] pmks = pm.cons.keys;
				final Object[] pmvs = pm.values;
				for (int i = 0; i < pmvs.length; i++) {
					final V value = (V) pmvs[i];
					if (value == NULL) continue;
					final String key = pmks[i];
					m.put(key, value);
				}
				return m;
			} else {
				ParameterMap<V> m = new ParameterMap<V>(this);
				for (Map.Entry<String, ? extends V> entry : map.entrySet()) {
					m.put(entry.getKey(), entry.getValue());
				}
				return m;
			}
		}
		
		/**
		 * The keys that are permitted for the {@link ParameterMap} instances created by the {@link Constructor}.
		 * @return the keys used to create the {@link Constructor}
		 */
		
		public Set<String> getKeys() {
			if (keySet != null) return keySet;
			return keySet = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(keys)));
		}
		
		int indexOf(Object obj) {
			return (obj instanceof String) ? indexOf((String) obj) : -1;
		}

		int indexOf(final String key) {
			final int index = mph.hashAsInt(key);
			return index >= 0 && keys[index].equals(key) ? index : -1;
		}
		
	}
	
}
