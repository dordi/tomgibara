package com.tomgibara.stupp;

import junit.framework.TestCase;

public class StuppTest extends TestCase {

	public void testSetKey() {
		StuppType type = StuppType.getInstance(Book.class);
		Object instance = type.newInstance();
		Stupp.setKey(instance, 1L);
		assertEquals(((Book)instance).getId(), 1L);
	}
	
	public void testSetInvalidKey() {
		StuppType type = StuppType.getInstance(Book.class);
		Object instance = type.newInstance();
		try {
			Stupp.setKey(instance, "1");
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
}
