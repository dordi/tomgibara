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

public class ProductLatticeTest extends TestCase {

	private static Set<Integer> set(Integer... integers) {
		return new HashSet<Integer>(Arrays.asList(integers));
	}
	
	private static Object[] tuple(Object... values) {
		return values;
	}

	public void testBounded() {
		ProductLattice lattice = new ProductLattice(new SetLattice<Integer>(set(1,2,3)), new OrderedLattice<Integer>());
		assertFalse(lattice.isBounded());
		assertFalse(lattice.isBoundedAbove());
		assertFalse(lattice.isBoundedBelow());
		
		Lattice<Object[]> above = lattice.boundedAbove(tuple(set(1,2), 3));
		assertFalse(above.isBounded());
		assertTrue(above.isBoundedAbove());
		assertFalse(above.isBoundedBelow());

		Lattice<Object[]> below = above.boundedBelow(tuple(set(1), 0));
		assertTrue(below.isBounded());
		assertTrue(below.isBoundedAbove());
		assertTrue(below.isBoundedBelow());
	}
	
	public void testCompare() {
		ProductLattice lattice = new ProductLattice(new SetLattice<Integer>(set(1,2,3)), new OrderedLattice<Integer>());
		assertEquals(Comparison.EQUAL, lattice.compare(tuple(set(1,2),2), tuple(set(1,2),2)));
		assertEquals(Comparison.LESS_THAN, lattice.compare(tuple(set(1,2),1), tuple(set(1,2),2)));
		assertEquals(Comparison.LESS_THAN, lattice.compare(tuple(set(1),2), tuple(set(1,2),2)));
		assertEquals(Comparison.LESS_THAN, lattice.compare(tuple(set(1),1), tuple(set(1,2),2)));
		assertEquals(Comparison.GREATER_THAN, lattice.compare(tuple(set(1,2),2), tuple(set(1,2),1)));
		assertEquals(Comparison.GREATER_THAN, lattice.compare(tuple(set(1,2),2), tuple(set(1),2)));
		assertEquals(Comparison.GREATER_THAN, lattice.compare(tuple(set(1,2),2), tuple(set(1),1)));
		assertEquals(Comparison.INCOMPARABLE, lattice.compare(tuple(set(1,2,3),1), tuple(set(1,2),2)));
	}
	
}
