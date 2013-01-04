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
package com.tomgibara.crinch.math;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

public class CombinatorTest extends TestCase {

	public void testGetSize() {
		assertEquals(new BigInteger("10"), new LongCombinator(5, 3).size());
	}
	
	public void testGetCombination() {
		testGetCombination(new LongCombinator(5, 3));
		testGetCombination(new LongCombinator(1, 1));
		testGetCombination(new LongCombinator(20, 10));
	}
	
	private void testGetCombination(Combinator c) {
		Set<String> values = new HashSet<String>();
		long size = c.size().longValue();
		for (long i = 0; i < size; i++) {
			//check the array contains distinct values
			final int[] arr = c.getCombination(i);
			Set<Integer> tmp = new HashSet<Integer>();
			for (int j : arr) assertTrue( tmp.add(j) );
			//check every combination is different
			assertTrue( values.add(Arrays.toString(arr)) );
		}
	}

	public void testConsistency() {
		testConsistency(new LongCombinator(5, 3), new BigIntCombinator(5, 3));
		testConsistency(new LongCombinator(20, 10), new BigIntCombinator(20, 10));
	}
	
	private void testConsistency(Combinator c1, Combinator c2) {
		assertEquals(c1, c2);
		assertEquals(c2, c1);
		assertEquals(c1.size(), c2.size());
		long size = c1.size().longValue();
		for (long i = 0; i < size; i++) {
			assertTrue(Arrays.equals(c1.getCombination(i), c2.getCombination(i)));
		}
	}

	public void testPackedConsistency() {
		testPackedConsistency(new LongCombinator(5, 3, true));
		testPackedConsistency(new LongCombinator(1000, 2, true));
	}
	
	public void testBitsPerElement() {
		assertEquals(3, new LongCombinator(5, 1, true).getBitsPerElement());
		assertEquals(10, new LongCombinator(1000, 1, true).getBitsPerElement());
		assertEquals(0, new LongCombinator(1, 1, true).getBitsPerElement());
		assertEquals(8, new LongCombinator(256, 8, true).getBitsPerElement());
		assertEquals(16, new LongCombinator(65536, 4, true).getBitsPerElement());
		assertEquals(17, new LongCombinator(65537, 1, true).getBitsPerElement());
	}
	
	private void testPackedConsistency(PackedCombinator c) {
		long size = c.size().longValue();
		for (long i = 0; i < size; i++) {
			int[] as = c.getCombination(i);
			long b = c.getPackedCombination(i);
			int t = c.getTupleLength();
			int[] bs = new int[t];
			for (int j = 0; j < t; j++) {
				bs[j] = c.getPackedElement(b, j);
			}
			assertTrue(Arrays.equals(as, bs));
		}
	}

	//to have any chance of being useful for hashing,
	//computing combinations should be faster than generating secure random nos.
	public void testSpeed() throws Exception {
		Random r = SecureRandom.getInstance("SHA1PRNG");

		Combinator c = new LongCombinator(200, 10);
		long limit = c.size().longValue();
		int count = 0;
		final int[] as = new int[c.getTupleLength()];
		long start1 = System.currentTimeMillis();
		for (long i = 0; i < limit; i += 100000000000L) {
			count++;
			c.getCombination(i, as);
		}
		long time1 = System.currentTimeMillis() - start1;
		
		long start2 = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			int[] cs = new int[10];
			for (int j = 0; j < cs.length; j++) {
				cs[j] = r.nextInt(200);
			}
		}
		long time2 = System.currentTimeMillis() - start2;
		assertTrue(time2 > time1);
		
	}

	public void testRandom() {
		int tests = 100000;
		Random r = new SecureRandom();
		Combinator c = new LongCombinator(20, 3);
		Map<IntArray, Integer> counts = new HashMap<CombinatorTest.IntArray, Integer>();
		for (int i = 0; i < tests; i++) {
			IntArray array = new IntArray(c.getRandomCombination(r));
			Integer count = counts.get(array);
			if (count == null) {
				counts.put(array, 1);
			} else {
				counts.put(array, count + 1);
			}
		}
		
		int sum = 0;
		int sumSqr = 0;
		for (Integer count : counts.values()) {
			int j = count;
			sum += j;
			sumSqr += j * j;
		}

		float size = c.size().longValue();
		float mean = sum / size;
		float meanSqr = sumSqr / size;
		float var = meanSqr - mean * mean;

		float p = 1f / size;
		float q = 1f - p;
		float v = tests * p * q;
		
		// expect to be within 10% of expected variance
		assertTrue(var > v * 0.9f);
		assertTrue(var < v * 1.1f);
	}
	
	private static final class IntArray {
		
		private final int[] ints;

		IntArray(int[] ints) {
			this.ints = ints;
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(ints);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof CombinatorTest.IntArray)) return false;
			CombinatorTest.IntArray that = (CombinatorTest.IntArray) obj;
			return Arrays.equals(this.ints, that.ints);
		}
		
		@Override
		public String toString() {
			return Arrays.toString(ints);
		}
		
	}
	
}
