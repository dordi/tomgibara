/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.collections;

import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.BitVector;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.ObjectHash;
import com.tomgibara.crinch.hashing.ObjectHashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.hashing.SingletonMultiHash;

public class BasicBloomFilterTest extends TestCase {

	static final int DEFAULT_MIN = 0;
	static final int DEFAULT_MAX = 999;
	static final int DEFAULT_SIZE = DEFAULT_MAX - DEFAULT_MIN + 1;
	
	MultiHash<Object> sha1Hash = new PRNGMultiHash<Object>("SHA1PRNG", new ObjectHashSource(), new HashRange(0, DEFAULT_SIZE - 1));
	MultiHash<Object> objHash = new SingletonMultiHash<Object>( Hashes.rangeAdjust(new HashRange(DEFAULT_MIN, DEFAULT_MAX), new SingletonMultiHash<Object>(new ObjectHash<Object>())) );

	public void testConstructorWithoutBitVector() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertEquals(0.0, bloom.getFalsePositiveProbability());
		assertEquals(10, bloom.getHashCount());
		assertEquals(sha1Hash, bloom.getMultiHash());
		assertEquals(true, bloom.isEmpty());
		assertEquals(DEFAULT_SIZE, bloom.getBitVector().size());
	}
	
	public void testConstructorWithBitVector() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(new BitVector(500), sha1Hash, 10);
		assertEquals(500, bloom.getCapacity());
		assertEquals(500, bloom.getMultiHash().getRange().getSize().intValue());
	}
	
	public void testConstructorWithImmutableBitVector() {
		try {
			new BasicBloomFilter<Object>(new BitVector(1000).immutableCopy(), sha1Hash, 10);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testIsEmpty() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertTrue(bloom.isEmpty());
		bloom.add(new Object());
		assertFalse(bloom.isEmpty());
		bloom.clear();
		assertTrue(bloom.isEmpty());
	}

	public void testGetFalsePositiveProbability() {
		int size = 10;
		MultiHash<Integer> multiHash = new SingletonMultiHash<Integer>( Hashes.rangeAdjust(new HashRange(0, size - 1), new SingletonMultiHash<Integer>(new ObjectHash<Integer>()) ) );
		BasicBloomFilter<Integer> bloom = new BasicBloomFilter<Integer>(multiHash, 1);
		double p = bloom.getFalsePositiveProbability();
		assertEquals(0.0, p);
		for (int i = 0; i < size; i++) {
			bloom.add(i);
			final double q = bloom.getFalsePositiveProbability();
			assertTrue(p < q);
			p = q;
		}
		assertEquals(1.0, p);
	}
	
	public void testClear() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertTrue(bloom.isEmpty());
		bloom.clear();
		assertTrue(bloom.isEmpty());
		bloom.add(new Object());
		assertFalse(bloom.isEmpty());
		bloom.clear();
		assertTrue(bloom.isEmpty());
	}
	
	public void testCapacity() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertEquals(sha1Hash.getRange().getSize().intValue(), bloom.getCapacity());
	}
	
	public void testEqualsAndHashCode() {
		BasicBloomFilter<Object> b1 = new BasicBloomFilter<Object>(sha1Hash, 1);
		BasicBloomFilter<Object> b2 = new BasicBloomFilter<Object>(sha1Hash, 1);
		BasicBloomFilter<Object> b3 = new BasicBloomFilter<Object>(sha1Hash, 2);
		BasicBloomFilter<Object> b4 = new BasicBloomFilter<Object>(objHash, 1);
		assertEquals(b1, b1);
		assertEquals(b1, b2);
		assertEquals(b2, b1);
		assertFalse(b1.equals(b3));
		assertFalse(b3.equals(b1));
		assertFalse(b1.equals(b4));
		assertFalse(b4.equals(b1));
		assertFalse(b3.equals(b4));
		assertFalse(b4.equals(b3));
	
		assertEquals(b1.hashCode(), b2.hashCode());
		
		final Object e = new Object();
		b1.add(e);
		assertFalse(b1.equals(b2));
		b2.add(e);
		assertTrue(b1.equals(b2));

		assertEquals(b1.hashCode(), b2.hashCode());
	}
	
	public void testAdd() {
		BasicBloomFilter<Object> bloom = new BasicBloomFilter<Object>(sha1Hash, 10);
		int bitCount = 0;
		for (int i = 0; i < 10; i++) {
			bloom.add(i);
			BasicBloomFilter<Object> b = bloom.clone();
			assertFalse(b.add(i));
			assertEquals(bloom, b);
			final int newBitCount = bloom.getBitVector().countOnes();
			assertTrue(newBitCount >= bitCount);
			bitCount = newBitCount;
		}
		for (int i = 0; i < 10; i++) {
			assertTrue(bloom.mightContain(i));
		}
	}
	
	public void testAddAll() {
		BasicBloomFilter<Object> b1 = new BasicBloomFilter<Object>(sha1Hash, 10);
		BasicBloomFilter<Object> b2 = new BasicBloomFilter<Object>(sha1Hash, 10);
		HashSet<Object> values = new HashSet<Object>();
		for (int i = 0; i < 10; i++) {
			b1.add(i);
			values.add(i);
		}
		b2.addAll(values);
		assertTrue(b1.equals(b2));
		assertFalse(b2.addAll(values));
		assertTrue(b1.equals(b2));
	}
	
	public void testMightContain() {
		BasicBloomFilter<Object> b = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertFalse(b.mightContain(new Object()));
		for (int i = 0; i < 10; i++) {
			if (b.clone().add(i)) assertFalse(b.mightContain(i));
			b.add(i);
			assertTrue(b.mightContain(i));
		}
	}
	
	public void testMightContainAll() {
		BasicBloomFilter<Object> b = new BasicBloomFilter<Object>(sha1Hash, 10);
		assertFalse(b.mightContainAll(Collections.singleton(new Object())));
		HashSet<Object> values = new HashSet<Object>();
		for (int i = 0; i < 10; i++) {
			b.add(i);
			values.add(i);
		}
		assertTrue(b.mightContainAll(values));
		for (int i = 10; i < 20; i++) {
			HashSet<Object> vs = new HashSet<Object>(values);
			if (b.clone().addAll(vs)) assertFalse(b.mightContainAll(vs));
		}
	}
}
