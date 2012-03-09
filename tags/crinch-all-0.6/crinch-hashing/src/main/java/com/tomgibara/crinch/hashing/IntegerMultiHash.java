/*
 * Copyright 2010 Tom Gibara, Benjamin Manes
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
package com.tomgibara.crinch.hashing;

import java.math.BigInteger;

/**
 * A MultiHash implementation that uses double-hashing to generate an arbitrary
 * number of hash-values based on an integer hash value. This implementation is
 * derived from
 * 
 * http://code.google.com/p/concurrentlinkedhashmap/wiki/BloomFilter
 * by Benjamin Manes.
 * 
 * @author tomgibara
 * 
 * @param <T>
 *            the type of objects for which hashes will be generated
 */

public class IntegerMultiHash<T> extends AbstractMultiHash<T> {

	private final Hash<T> hash;
	private final HashRange range;
	private final int size;

	public IntegerMultiHash(Hash<T> hash, int max) {
		if (hash == null) throw new IllegalArgumentException("null hash");
		if (!hash.getRange().equals(HashRange.FULL_INT_RANGE)) throw new IllegalArgumentException("hash does not have full integer range");
		if (max < 0) throw new IllegalArgumentException();
		this.hash = hash;
		range = new HashRange(0, max);
		size = range.getSize().intValue();
	}
	
	@Override
	public HashRange getRange() {
		return range;
	}

	@Override
	public int getMaxMultiplicity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int[] hashAsInts(T value, int[] array) {
        // Double hashing allows calculating multiple index locations
        int hashCode = hash.hashAsInt(value);
        int probe = 1 + Math.abs(hashCode % size);
        int h = spread(hashCode);
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.abs(h ^ i * probe) % size;
        }
        return array;
	}
	
	@Override
	public long[] hashAsLongs(T value, long[] array) {
		return copy(hashAsInts(value, array.length), array);
	}
	
	@Override
	public BigInteger[] hashAsBigInts(T value, BigInteger[] array) {
		return copy(hashAsInts(value, array.length), array);
	}
	
    private int spread(int hashCode) {
        // Spread bits using variant of single-word Wang/Jenkins hash
        hashCode += (hashCode <<  15) ^ 0xffffcd7d;
        hashCode ^= (hashCode >>> 10);
        hashCode += (hashCode <<   3);
        hashCode ^= (hashCode >>>  6);
        hashCode += (hashCode <<   2) + (hashCode << 14);
        return hashCode ^ (hashCode >>> 16);
    }
    
    @Override
    public int hashCode() {
    	return hash.hashCode() ^ size;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj == this) return true;
    	if (!(obj instanceof IntegerMultiHash<?>)) return false;
    	IntegerMultiHash<?> that = (IntegerMultiHash<?>) obj;
    	if (this.size != that.size) return false;
    	if (!this.hash.equals(that.hash)) return false;
    	return true;
    }
    
    @Override
    public String toString() {
    	return "ObjectMultiHash size: " + size;
    }
    
    
}
