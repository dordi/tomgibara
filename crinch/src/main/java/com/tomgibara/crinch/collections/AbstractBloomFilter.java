package com.tomgibara.crinch.collections;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashList;

public abstract class AbstractBloomFilter<E> implements BloomFilter<E> {

	@Override
	public boolean isEmpty() {
		return getBits().isAllZeros();
	}
	
	@Override
	public double getFalsePositiveProbability() {
		return Math.pow( (double) getBits().countOnes() / getBits().size(), getHashCount());
	}
	
	@Override
	public int getCapacity() {
		return getBits().size();
	}
	
	@Override
	public boolean addAll(Iterable<? extends E> elements) {
		boolean mutated = false;
		for (E element : elements) if ( add(element) ) mutated = true;
		return mutated;
	}

	@Override
	public boolean mightContain(E element) {
		final int hashCount = getHashCount();
		final HashList hashList = getMultiHash().hashAsList(element, hashCount);
		final BitVector bitVector = getBits();
		for (int i = 0; i < hashCount; i++) {
			if (!bitVector.getBit( hashList.getAsInt(i) )) return false;
		}
		return true;
	}
	
	@Override
	public boolean mightContainAll(Iterable<? extends E> elements) {
		for (E element : elements) if (!mightContain(element)) return false;
		return true;
	}

	@Override
	public boolean containsAll(BloomFilter<?> filter) {
		checkCompatible(filter);
		return getBits().testContains(filter.getBits());
	}

	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BloomFilter<?>)) return false;
		final BloomFilter<?> that = (BloomFilter<?>) obj;
		if (this.getHashCount() != that.getHashCount()) return false;
		if (!this.getMultiHash().equals(that.getMultiHash())) return false;
		if (!this.getBits().equals(that.getBits())) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return getBits().hashCode();
	}
	
	@Override
	public String toString() {
		return getBits().toString();
	}

	// package scoped methods

	void checkCompatible(BloomFilter<?> that) {
		if (this.getHashCount() != that.getHashCount()) throw new IllegalArgumentException("Incompatible filter, hashCount was " + that.getHashCount() +", expected " + this.getHashCount());
		if (!this.getMultiHash().equals(that.getMultiHash())) throw new IllegalArgumentException("Incompatible filter, multiHashes were not equal");
	}
	
}
