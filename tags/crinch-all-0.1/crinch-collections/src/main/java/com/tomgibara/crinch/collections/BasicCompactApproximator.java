/*
 * Copyright 2011 Tom Gibara
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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.lattice.Lattice;

public class BasicCompactApproximator<K,V> implements CompactApproximator<K, V>, Cloneable {

	private final ModCount modCount;
	private final MultiHash<? super K> multiHash;
	private final int hashCount;
	private final int[] hashes;
	private final Lattice<V> storeLattice;
	private final Lattice<V> accessLattice;
	private final V[] values;
	private final ValueList<V> valueList;
	private CompactBloomFilter bloomFilter = null;

	public BasicCompactApproximator(Lattice<V> lattice, MultiHash<? super K> multiHash, int hashCount) {
		if (!lattice.isBoundedBelow()) throw new IllegalArgumentException("lattice not bounded below");

		final HashRange range = multiHash.getRange();
		if (range == null) throw new IllegalArgumentException("null multiHash range");
		if (!range.isIntBounded()) throw new IllegalArgumentException("multiHash not int bounded");
		multiHash = Hashes.rangeAdjust(range.zeroBased(), multiHash);

		this.modCount = new ModCount();
		this.storeLattice = lattice;
		this.accessLattice = lattice;
		this.multiHash = multiHash;
		this.hashCount = hashCount;
		hashes = new int[hashCount];
		//TODO nasty - should use Array.newInstance() ?
		values = (V[]) new Object[ multiHash.getRange().getSize().intValue() ];
		clear();
		valueList = createValueList(values);
	}
	
	private BasicCompactApproximator(BasicCompactApproximator<K, V> that) {
		modCount = new ModCount();
		storeLattice = that.storeLattice;
		accessLattice = that.accessLattice;
		multiHash = that.multiHash;
		hashCount = that.hashCount;
		hashes = new int[hashCount];
		values = that.values.clone();
		valueList = createValueList(values);
	}

	private BasicCompactApproximator(BasicCompactApproximator<K, V> that, Lattice<V> accessLattice) {
		modCount = that.modCount;
		storeLattice = that.storeLattice;
		this.accessLattice = accessLattice;
		multiHash = that.multiHash;
		hashCount = that.hashCount;
		hashes = new int[hashCount];
		values = that.values.clone();
		valueList = createValueList(values);
	}

	public V put(K key, V value) {
		if (!accessLattice.contains(value)) throw new IllegalArgumentException();
		final int[] hashes = multiHash.hashAsInts(key, this.hashes);
		V previous = accessLattice.getTop();
		for (int i = 0; i < hashCount; i++) {
			final int hash = hashes[i];
			final V v = values[hash];
			previous = storeLattice.meet(previous, v);
			values[hash] = storeLattice.join(value, v);
		}
		//assumes putting has resulted in a change
		modCount.count++;
		return previous;
	}
	
	public V getSupremum(K key) {
		final int[] hashes = multiHash.hashAsInts(key, this.hashes);
		V value = accessLattice.getTop();
		for (int i = 0; i < hashCount; i++) {
			final V v = values[hashes[i]];
			value = storeLattice.meet(value, v);
		}
		return value;
	}

	public boolean mightContain(K key) {
		final int[] hashes = multiHash.hashAsInts(key, this.hashes);
		V bottom = storeLattice.getBottom();
		for (int i = 0; i < hashCount; i++) {
			if (storeLattice.equalInLattice(values[hashes[i]], bottom)) return false;
		}
		return true;
	}
	
	@Override
	public boolean mightContainAll(Iterable<? extends K> keys) {
		for (K key : keys) if (!mightContain(key)) return false;
		return true;
	}
	
	public void clear() {
		Arrays.fill(values, storeLattice.getBottom());
		modCount.count++;
	}

	public boolean isEmpty() {
		final V bottom = storeLattice.getBottom();
		for (V value : values) {
			if (!storeLattice.equalInLattice(value, bottom)) return false;
		}
		return true;
	}
	
	@Override
	public boolean bounds(CompactApproximator<K, V> that) {
		checkCompatibility(that);
		final ValueList<V> thisValues = this.valueList;
		final List<V> thatValues = (List<V>) that.getValueList();
		final int size = thisValues.size();
		if (thatValues instanceof RandomAccess) {
			for (int i = 0; i < size; i++) {
				if (!storeLattice.isOrdered(thatValues.get(i), thisValues.get(i))) return false;
			}
		} else {
			final Iterator<V> it = thatValues.iterator();
			for (int i = 0; i < size; i++) {
				if (!storeLattice.isOrdered(it.next(), thisValues.get(i))) return false;
			}
		}
		return true;
	}
	
	@Override
	public CompactApproximator<K,V> boundedAbove(V upperBound) {
		final Lattice<V> subLattice = accessLattice.boundedAbove(upperBound);
		return subLattice.equals(accessLattice) ? this : new BasicCompactApproximator<K, V>(this, subLattice);
	}
	
	@Override
	public BloomFilter<K> asBloomFilter() {
		return bloomFilter == null ? bloomFilter = new CompactBloomFilter() : bloomFilter;
	}

	@Override
	public Lattice<V> getLattice() {
		return accessLattice;
	}
	
	public int getCapacity() {
		return values.length;
	}
	
	public int getHashCount() {
		return hashCount;
	}
	
	public MultiHash<? super K> getMultiHash() {
		return multiHash;
	}

	@Override
	public List<V> getValueList() {
		return valueList;
	}
	
	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BasicCompactApproximator<?, ?>)) return false;
		BasicCompactApproximator<?, ?> that = (BasicCompactApproximator<?, ?>) obj;
		if (this.getHashCount() != that.getHashCount()) return false;
		if (!this.getMultiHash().equals(that.getMultiHash())) return false;
		if (!this.getLattice().equals(that.getLattice())) return false;
		if (!this.getValueList().equals(that.getValueList())) return false;
		/*
		 * This idea doesn't work, because how do you produce a consistent hashcode - create a bounded lattice with same top & bottom?
		 * must simply be a rule that for equality to be defined, equality in lattice must be consistent with object equality
		 */
		/*
		//compare values as per lattice, not equality
		final V[] thisValues = this.values;
		final List<V> thatValues = (List<V>) that.getValueList();
		//TODO should Lattice.contains be much more forgiving?
		if (thatValues instanceof RandomAccess) {
			for (int i = 0; i < thisValues.length; i++) {
				if (!storeLattice.equalInLattice(thisValues[i], thatValues.get(i))) return false;
			}
		} else {
			final Iterator<V> it = thatValues.iterator();
			for (int i = 0; i < thisValues.length; i++) {
				if (!storeLattice.equalInLattice(thisValues[i], it.next())) return false;
			}
		}
		*/
		return true;
	}

	@Override
	public int hashCode() {
		return getValueList().hashCode();
	}
	
	@Override
	public String toString() {
		return getValueList().toString();
	}
	
	@Override
	public BasicCompactApproximator<K, V> clone() {
		return new BasicCompactApproximator<K, V>(this);
	}
	
	private  ValueList<V> createValueList(V[] values) {
		return storeLattice.equals(accessLattice) ?
			new ValueList<V>(values) :
			new ValueList<V>(values, storeLattice, accessLattice.getTop());
	}
	
	private void checkCompatibility(CompactApproximator<K, V> that) {
		if (this.hashCount != that.getHashCount()) throw new IllegalArgumentException("Incompatible compact approximator, hashCount was " + that.getHashCount() +", expected " + hashCount);
		if (!this.multiHash.equals(that.getMultiHash())) throw new IllegalArgumentException("Incompatible compact approximator, multiHashes were not equal.");
		if (!this.accessLattice.equals(that.getLattice())) throw new IllegalArgumentException("Incompatible compact approximator, lattices were not equal.");
	}

	private static class ModCount {
		
		volatile int count = 0;
		
	}
	
	private static class ValueList<E> extends AbstractList<E> {

		private final E[] values;
		private final Lattice<E> lattice;
		private final E newTop;
		
		public ValueList(E[] values) {
			this.values = values;
			lattice = null;
			newTop = null;
		}

		public ValueList(E[] values, Lattice<E> lattice, E newTop) {
			this.values = values;
			this.lattice = lattice;
			this.newTop = newTop;
		}

		@Override
		public E get(int index) {
			if (index < 0 || index >= values.length) throw new IndexOutOfBoundsException();
			return lattice == null ? values[index] : lattice.meet(newTop, values[index]);
		}

		@Override
		public int size() {
			return values.length;
		}
		
	}

	private class CompactBloomFilter extends AbstractBloomFilter<K> {

		int myModCount = modCount.count - 1;
		final BitVector bitVector = new BitVector(values.length);
		final BitVector publicBitVector = bitVector.immutableView();
		//cached values
		final V top = accessLattice.getTop();
		
		@Override
		public boolean add(K key) {
			return !accessLattice.equalInLattice(top, put(key, top));
		}

		@Override
		public boolean addAll(BloomFilter<? extends K> filter) {
			checkCompatible(filter);
			updateBitVector();
			final BitVector thisBits = bitVector;
			final BitVector thatBits = filter.getBitVector();
			if (thisBits.testContains(thatBits)) return false;
			//we can trounce the bit set now because we know modifications will be made
			thisBits.flip();
			thisBits.andVector(thatBits);
			for (int i = thisBits.firstOne(); i >= 0; i = thisBits.nextOne(i+1)) {
				values[i] = storeLattice.join(top, values[i]);
			}
			modCount.count++;
			return true;
		}

		@Override
		public void clear() {
			BasicCompactApproximator.this.clear();
		}

		@Override
		public BitVector getBitVector() {
			updateBitVector();
			return publicBitVector;
		}

		@Override
		public int getHashCount() {
			return hashCount;
		}

		@Override
		public MultiHash<? super K> getMultiHash() {
			return multiHash;
		}

		private void updateBitVector() {
			final int count = modCount.count;
			if (myModCount != count) {
				myModCount = count;
				final Lattice<V> lattice = storeLattice;
				final int size = values.length;
				for (int i = 0; i < size; i++) {
					bitVector.setBit(i, lattice.isOrdered(top, values[i]));
				}
			}
		}
		
	}
	
}
