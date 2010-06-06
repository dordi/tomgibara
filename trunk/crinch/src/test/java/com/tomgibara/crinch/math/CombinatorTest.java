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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.BitVector;

public class CombinatorTest extends TestCase {

	public void testGetSize() {
		assertEquals(new BigInteger("10"), new LongCombinator(5, 3).getSize());
	}
	
	public void testGetCombination() {
		testGetCombination(5, 3);
		testGetCombination(1, 1);
		testGetCombination(20, 10);
	}
	
	private void testGetCombination(int n, int k) {
		Combinator c = new LongCombinator(n, k);
		Set<String> values = new HashSet<String>();
		long size = c.getSize().longValue();
		for (long i = 0; i < size; i++) {
			//check the array contains distinct values
			final int[] arr = c.getCombination(i);
			Set<Integer> tmp = new HashSet<Integer>();
			for (int j : arr) assertTrue( tmp.add(j) );
			//check every combination is different
			assertTrue( values.add(Arrays.toString(arr)) );
		}
	}

	//to have any chance of being useful for hashing,
	//computing combinations should be faster than generating secure random nos.
	public void testSpeed() throws Exception {
		Random r = SecureRandom.getInstance("SHA1PRNG");

		Combinator c = new LongCombinator(200, 10);
		long limit = c.getSize().longValue();
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

}
