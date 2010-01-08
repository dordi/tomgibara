/*
 * Copyright 2009 Tom Gibara
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
package com.tomgibara.stupp;

import java.util.HashSet;

import junit.framework.TestCase;

public class ScopeTest extends TestCase {

	public void testPropertyScope() {

		final StuppScope scope = new StuppScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		StuppType authorType = StuppType.getInstance(Author.class);

		Book book = bookFactory.newInstance(1L);
		Author author = (Author) authorType.newInstance();
		author.setId(2L);
		book.setAuthor(author);
		assertEquals(scope, Stupp.getScope(author));
	}

	public void testNullKey() {
		final StuppScope scope = new StuppScope();
		final StuppType bookType = StuppType.getInstance(Book.class);
		try {
			scope.attach(bookType.newInstance());
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testNullPropertyKey() {

		final StuppScope scope = new StuppScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		final StuppType authorType = StuppType.getInstance(Author.class);

		Book book = bookFactory.newInstance(1L);
		Author author = (Author) authorType.newInstance();
		try {
			book.setAuthor(author);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}

	}
	
	public void testTransitiveScopeChange() throws Exception {

		final StuppScope scope1 = new StuppScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope1);
		StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(StuppType.getInstance(Author.class), scope1);
		
		Book book = bookFactory.newInstance(1L);
		Author author = authorFactory.newInstance(2L);
		book.setAuthor(author);
		
		scope1.detach(book);
		assertNull(Stupp.getScope(book));
		assertNull(Stupp.getScope(author));
		
		final StuppScope scope2 = new StuppScope();
		scope2.attach(book);
		assertEquals(scope2, Stupp.getScope(book));
		assertEquals(scope2, Stupp.getScope(author));
	}

	public void testCollection() {
		final StuppScope bookScope = new StuppScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), bookScope);
		final StuppScope pubScope = new StuppScope();
		StuppFactory<Publisher, Integer> pubFactory = new StuppFactory<Publisher, Integer>(StuppType.getInstance(Publisher.class), pubScope);
		Publisher publisher = pubFactory.newInstance(1);
		Book book1 = bookFactory.newInstance(1L);
		Book book2 = bookFactory.newInstance(2L);
		Book book3 = bookFactory.newInstance(3L);
		book1.setName("Fundamental Algorithms");
		book2.setName("Seminumerical Algorithms");
		book3.setName("Sorting and Searching");
		HashSet<Book> books = new HashSet<Book>();
		books.add(book1);
		books.add(book2);
		books.add(book3);
		publisher.setBooks(books);
		assertEquals(3, books.size());
		for (Book book : books) {
			assertEquals(pubScope, Stupp.getScope(book));
		}
	}
	
	public void testSameTypesDifferentKey() {

		final StuppScope scope = new StuppScope();
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppType authorType = StuppType.getInstance(Author.class);
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope);
		final StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(authorType, scope);
		
		final Book book = bookFactory.newInstance(1L);
		final Author author = authorFactory.newInstance(1L);

		final StuppIndex<StuppTuple> bookIndex = scope.getPrimaryIndex(bookType);
		final StuppIndex<StuppTuple> authorIndex = scope.getPrimaryIndex(authorType);
		assertEquals(book, bookIndex.getSingle(bookIndex.getProperties().tupleFromValues(1L)));
		assertEquals(author, authorIndex.getSingle(authorIndex.getProperties().tupleFromValues(1L)));
		
	}
	
	public void testSameTypeSameKey() {
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), new StuppScope());
		final Book book = bookFactory.newInstance(1L);
		try {
			bookFactory.newInstance(1L);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
	}

	public void testChangeKey() {
		final StuppScope scope = new StuppScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		final Book bookA = bookFactory.newInstance(1L);
		bookA.setId(1L);
		assertEquals(1L, bookA.getId());
		assertEquals(bookA, bookFactory.getInstance(1L));
		bookA.setId(2L);
		assertEquals(2L, bookA.getId());
		assertEquals(bookA, bookFactory.getInstance(2L));
		final Book bookB = bookFactory.newInstance(1L);
		assertEquals(1L, bookB.getId());
		assertEquals(bookB, bookFactory.getInstance(1L));
		try {
			bookA.setId(1L);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testChangeKeyToDuplicate() {
		final StuppScope scope = new StuppScope();
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		final StuppProperties indexProps = scope.getPrimaryIndex(type).getProperties();
		final Book bookA = bookFactory.newInstance(1L);
		final Book bookB = bookFactory.newInstance(2L);
		try {
			bookA.setId(2);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		assertEquals(indexProps.tupleFromValues(1L), type.getIndexProperties().tupleFromInstance(bookA));
		assertEquals(scope, Stupp.getScope(bookA));
		assertEquals(indexProps.tupleFromValues(2L), type.getIndexProperties().tupleFromInstance(bookB));
		assertEquals(scope, Stupp.getScope(bookB));
	}
	
	/*
	public void testKeyedKey() {
		final StuppScope scope = new StuppScope();
		final StuppFactory<Jacket, Book> jacketFactory = new StuppFactory<Jacket, Book>(StuppType.getInstance(Jacket.class), scope);
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), scope);
		final Book book = bookFactory.newInstance(1L);
		try {
			jacketFactory.newInstance(book);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected *-/
		}
	}
	*/

	public void testGet() {
		final StuppScope scope = new StuppScope();
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope);
		final StuppType authorType = StuppType.getInstance(Author.class);
		final StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(authorType, scope);
		final StuppType pubType = StuppType.getInstance(Publisher.class);
		final StuppFactory<Publisher, Integer> pubFactory = new StuppFactory<Publisher, Integer>(pubType, scope);

		assertEquals(0, scope.getPrimaryIndex(bookType).getAll().size());
		assertEquals(0, scope.getPrimaryIndex(authorType).getAll().size());
		assertEquals(0, scope.getPrimaryIndex(pubType).getAll().size());

		assertEquals(0, scope.getAllObjects().size());
		Book book = bookFactory.newInstance(1L);
		assertEquals(1, scope.getAllObjects().size());
		Author author1 = authorFactory.newInstance(1L);
		assertEquals(2, scope.getAllObjects().size());
		Author author2 = authorFactory.newInstance(2L);
		assertEquals(3, scope.getAllObjects().size());
		
		assertEquals(1, scope.getPrimaryIndex(bookType).getAll().size());
		assertEquals(2, scope.getPrimaryIndex(authorType).getAll().size());
		assertEquals(0, scope.getPrimaryIndex(pubType).getAll().size());
	}

	public void testAddIndex() {
		final StuppScope scope = new StuppScope();
		final StuppType type = StuppType.getInstance(Book.class);
		scope.register(type);
		final StuppPropertyIndex index = new StuppPropertyIndex(type.properties("name"), "test");
		scope.addIndex(index);
		assertEquals(2, scope.getAllIndices().size());
		assertTrue(scope.getAllIndices().contains(index));
		assertEquals(1, scope.getIndices(type, "name").size());
		assertSame(index, scope.getIndices(type, "name").iterator().next());
		try {
			scope.addIndex(index);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		//TODO test adding index attached to other scope
	}

	public void testRemoveIndex() {
		final StuppScope scope = new StuppScope();
		final StuppType type = StuppType.getInstance(Book.class);
		scope.register(type);
		final StuppPropertyIndex index = new StuppPropertyIndex(type.properties("name"), "test");
		scope.addIndex(index);
		scope.removeIndex(index);
		assertEquals(1, scope.getAllIndices().size());
		assertFalse(scope.getAllIndices().contains(index));
	}

	public void testDetachAll() {
		final StuppScope scope = new StuppScope();
		//check safe on totally empty
		scope.detachAll();
		assertEquals(0, scope.getAllObjects().size());
		final StuppType type = StuppType.getInstance(Book.class);
		scope.register(type);
		Book book = (Book) type.newInstance();
		book.setId(1L);
		scope.attach(book);
		assertEquals(1, scope.getAllObjects().size());
		//check clearance
		scope.detachAll();
		assertEquals(0, scope.getAllObjects().size());
		//check idempotent
		scope.detachAll();
		assertEquals(0, scope.getAllObjects().size());
	}
	
	private static interface Jacket {
		
		@StuppIndexed
		void setBook(Book book);
		
	}
	
}
