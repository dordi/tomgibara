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

/**
 * A MultiHash implementation that uses double-hashing to generate an arbitrary
 * number of hash-values based on the hashCode of an object. This implementation
 * is derived from
 * 
 * http://code.google.com/p/concurrentlinkedhashmap/wiki/BloomFilter
 * by Benjamin Manes.
 * 
 * @author tomgibara
 * 
 * @param <T>
 *            the type of objects for which hashes will be generated
 */

public class ObjectMultiHash<T> extends AbstractMultiHash<T> {

	private final HashRange range;
	private final int size;
	
	public ObjectMultiHash(int max) {
		if (max < 0) throw new IllegalArgumentException();
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
	public HashList hashAsList(T value, int multiplicity) {
        // Double hashing allows calculating multiple index locations
        int hashCode = value.hashCode();
        int probe = 1 + Math.abs(hashCode % size);
        int[] indexes = new int[multiplicity];
        int h = spread(hashCode);
        for (int i = 0; i < multiplicity; i++) {
            indexes[i] = Math.abs(h ^ i * probe) % size;
        }
        return Hashes.asHashList(indexes);
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
}
