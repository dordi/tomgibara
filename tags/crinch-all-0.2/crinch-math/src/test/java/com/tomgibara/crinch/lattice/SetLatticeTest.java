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
package com.tomgibara.crinch.lattice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tomgibara.crinch.poset.PartialOrder.Comparison;

import junit.framework.TestCase;

public class SetLatticeTest extends TestCase {

	private static Set<Integer> set(Integer... integers) {
		return new HashSet<Integer>(Arrays.asList(integers));
	}
	
	public void testBounded() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(1,2,3,4), lattice.getTop());
		assertEquals(set(), lattice.getBottom());
		Lattice<Set<Integer>> bounded = lattice.bounded(set(1,2,4), set(2));
		assertTrue(bounded.contains(set(1,2)));
		assertTrue(bounded.contains(set(2,4)));
		assertFalse(bounded.contains(set(1,4)));
	}
	
	public void testMeet() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(2,3), lattice.meet(set(1,2,3), set(2,3,4)));
		assertEquals(set(1), lattice.meet(set(1,2,3), set(1)));
		assertEquals(set(1), lattice.meet(set(1), set(1,2,3)));
		assertEquals(set(), lattice.meet(set(1), set(2)));
		assertEquals(set(), lattice.meet(set(1), set()));
		assertEquals(set(), lattice.meet(set(), set(1)));
	}
	
	public void testJoin() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(set(1,2,3,4), lattice.join(set(1,2,3), set(2,3,4)));
		assertEquals(set(1,2,3), lattice.join(set(1,2,3), set(1)));
		assertEquals(set(1,2,3), lattice.join(set(1), set(1,2,3)));
		assertEquals(set(1,2), lattice.join(set(1), set(2)));
		assertEquals(set(1), lattice.join(set(1), set()));
		assertEquals(set(1), lattice.join(set(), set(1)));
	}
	
	public void testCompare() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertEquals(Comparison.EQUAL, lattice.compare(set(1,2), set(1,2)));
		assertEquals(Comparison.LESS_THAN, lattice.compare(set(1,2), set(1,2,3)));
		assertEquals(Comparison.GREATER_THAN, lattice.compare(set(1,2,3), set(1,2)));
		assertEquals(Comparison.INCOMPARABLE, lattice.compare(set(1,2), set(2,3)));
	}
	
	public void testIsOrdered() {
		SetLattice<Integer> lattice = new SetLattice<Integer>(set(1,2,3,4));
		assertTrue(lattice.isOrdered(set(1,2), set(1,2)));
		assertTrue(lattice.isOrdered(set(1,2), set(1,2,3)));
		assertFalse(lattice.isOrdered(set(1,2,3), set(1,2)));
		assertFalse(lattice.isOrdered(set(2,3), set(1,2)));
	}
}
