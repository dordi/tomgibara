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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.MultiHash;
import com.tomgibara.crinch.hashing.ObjectHashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.lattice.Lattice;
import com.tomgibara.crinch.lattice.SetLattice;

import junit.framework.TestCase;

public class BasicCompactApproximatorTest extends TestCase {

	private static Set<Integer> set(Integer... integers) {
		return new HashSet<Integer>(Arrays.asList(integers));
	}

	public void testSet() {
		int size = 1000;
		MultiHash<Object> hash = new PRNGMultiHash<Object>("SHA1PRNG", new ObjectHashSource(), new HashRange(0, size - 1));
		Lattice<Set<Integer>> lattice = new SetLattice<Integer>(set(1,2,3,4));
		CompactApproximator<String, Set<Integer>> ca = new BasicCompactApproximator<String, Set<Integer>>(lattice, hash, 10);
		assertTrue(ca.isEmpty());
		ca.put("first", set(1));
		assertFalse(ca.isEmpty());
		assertEquals(set(1), ca.getSupremum("first"));
		ca.put("first", set(2));
		assertEquals(set(1,2), ca.getSupremum("first"));

		int count = 100;
		Random r = new Random();
		HashMap<String, Set<Integer>> in = new HashMap<String, Set<Integer>>();
		for (int i = 0; i < count; i++) {
			HashSet<Integer> set = new HashSet<Integer>();
			int x = r.nextInt();
			for (int j = 0; j < 4; j++) {
				if ((x & (1 << j)) != 0) set.add(j+1);
			}
			final String s = String.valueOf(i);
			in.put(s, set);
			ca.put(s, set);
		}
		
		for (int i = 0; i < count; i++) {
			final String s = String.valueOf(i);
			Set<Integer> supremum = ca.getSupremum(s);
			Set<Integer> set = in.get(s);
			assertTrue(lattice.join(set, supremum).equals(supremum));
//			if (!set.equals(supremum)) {
//				HashSet<Integer> diff = new HashSet<Integer>(supremum);
//				diff.removeAll(set);
//				System.out.println(s + " " + diff);
//			}
		}
		
		CompactApproximator<String, Set<Integer>> ca2 = new BasicCompactApproximator<String, Set<Integer>>(lattice, hash, 10);
		for (Map.Entry<String, Set<Integer>> entry : in.entrySet()) {
			ca2.put(entry.getKey(), entry.getValue());
		}
		assertTrue(ca.bounds(ca2));
		
		Set<Integer> bound = set(1,2);
		CompactApproximator<String, Set<Integer>> cab = ca.boundedAbove(bound);
		assertEquals(bound, cab.getLattice().getTop());
		for (String key : in.keySet()) {
			if (ca.mightContain(key)) assertTrue(cab.mightContain(key));
			assertTrue(bound.containsAll(cab.getSupremum(key)));
		}
		
		BloomFilter<String> bf = ca.asBloomFilter();
		for (String key : in.keySet()) {
			if (bf.mightContain(key)) {
				assertTrue(lattice.isOrdered(bound, ca.getSupremum(key)));
			}
		}
		
	}
	
}
