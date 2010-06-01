package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * A {@link MultiHash} implementation that returns the single hash value
 * produced by an arbitrary {@link Hash} instance.
 * 
 * @author tomgibara
 * 
 * @param <T>
 *            the type of objects for which hashes may be generated
 */

public class SingletonMultiHash<T> extends AbstractMultiHash<T> {

	// fields
	
	private final Hash<T> hash;

	// constructors
	
	public SingletonMultiHash(Hash<T> hash) {
		if (hash == null) throw new IllegalArgumentException("null hash");
		this.hash = hash;
	}
	
	// hash methods
	
	@Override
	public HashRange getRange() {
		return hash.getRange();
	}

	@Override
	public BigInteger hashAsBigInt(T value) {
		return hash.hashAsBigInt(value);
	}

	@Override
	public int hashAsInt(T value) {
		return hash.hashAsInt(value);
	}

	@Override
	public long hashAsLong(T value) {
		return hash.hashAsLong(value);
	}
	
	// object methods
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof SingletonMultiHash<?>)) return false;
		SingletonMultiHash<T> that = (SingletonMultiHash<T>) obj;
		return this.hash.equals(that.hash);
	}
	
	@Override
	public int hashCode() {
		return hash.hashCode();
	}
	
	@Override
	public String toString() {
		return "Multi: " + hash;
	}

}
