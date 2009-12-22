package com.tomgibara.stupp;

import java.util.Collection;

import junit.framework.TestCase;

public class UniqueIndexTest extends TestCase {

	public void testUniqueNullable() {
		
		final StuppScope scope = new StuppScope();
		StuppType type = StuppType.getInstance(Book.class);
		scope.register(type);
		final StuppUniqueIndex index = new StuppUniqueIndex(new StuppProperties(type, "name"), false);
		scope.addIndex(index);

		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		Book book1 = bookFactory.newInstance(1L);
		book1.setName("Tom");
		assertSame(book1, index.getSingleForKey("Tom"));
		assertNull(index.getSingleForKey("Jack"));
		book1.setName("Jack");
		assertSame(book1, index.getSingleForKey("Jack"));
		Book book2 = bookFactory.newInstance(2L);
		try {
			book2.setName("Jack");
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		book2.setName("Tom");
		assertSame(book2, index.getSingleForKey("Tom"));
	}
	
}
