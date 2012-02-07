package com.tomgibara.crinch.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

public class RangedIntSetTest extends TestCase {

	public void testBasic() throws Exception {
		RangedIntSet set = new RangedIntSet(10, 20);
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
		set.remove(null);
		set.remove(1);
		set.retainAll(Collections.singleton("STR"));
		set.add(10);
		assertEquals(1, set.size());
		assertEquals(set, Collections.singleton(10));
		set.retainAll(Collections.singleton(10));
		assertFalse(set.isEmpty());
		assertTrue(set.iterator().hasNext());
		assertEquals(10, (int) set.iterator().next());
		set.addAll(Arrays.asList(15, 18));
		assertEquals(3, set.size());
		assertEquals(10, (int) set.first());
		assertEquals(18, (int) set.last());
		assertEquals(Collections.singleton(10), set.headSet(15));
		assertEquals(new HashSet<Integer>(Arrays.asList(15, 18)), set.tailSet(15));
		assertEquals(Collections.singleton(15), set.subSet(13, 17));
	}
	
}
