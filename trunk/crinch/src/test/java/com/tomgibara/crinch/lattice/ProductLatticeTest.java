package com.tomgibara.crinch.lattice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	
}
