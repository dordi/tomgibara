package com.tomgibara.stupp;

import java.util.Collection;

import junit.framework.TestCase;

public class PropertyIndexTest extends TestCase {

	public void testProperty() {
		final StuppScope scope = new StuppScope();
		StuppType type = StuppType.getInstance(Book.class);
		final StuppPropertyIndex index = new StuppPropertyIndex(new StuppProperties(type, "name"));
		scope.addIndex(index);
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		Book book1 = bookFactory.newInstance(1L);
		book1.setName("Tom");
		assertSame(book1, index.getSingleForValue("Tom"));
		assertNull(index.getSingleForValue("Jack"));
		book1.setName("Jack");
		assertSame(book1, index.getSingleForValue("Jack"));
		Book book2 = bookFactory.newInstance(2L);
		book2.setName("Jack");
		Collection<Object> books = index.getForValue("Jack");
		assertTrue(books.contains(book1));
		assertEquals(2, books.size());
	}

}
