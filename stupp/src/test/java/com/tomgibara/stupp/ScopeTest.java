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
import java.util.Map;

import com.tomgibara.stupp.ann.StuppIndexed;

import junit.framework.TestCase;

public class ScopeTest extends TestCase {

	public void testPropertyScope() {
		final StuppType bookType = StuppType.getInstance(Book.class);
		StuppType authorType = StuppType.getInstance(Author.class);
		final StuppScope scope = StuppScope.newDefinition().addType(bookType).addType(authorType).createScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope);

		Book book = bookFactory.newInstance(1L);
		Author author = (Author) authorType.newInstance();
		author.setId(2L);
		book.setAuthor(author);
		assertEquals(scope, Stupp.getScope(author));
	}

	public void testNullKey() {
		final StuppScope scope = StuppScope.newDefinition().createScope();
		final StuppType bookType = StuppType.getInstance(Book.class);
		try {
			scope.attach(bookType.newInstance());
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testNullPropertyKey() {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition().addType(type).createScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
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
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppType authorType = StuppType.getInstance(Author.class);

		final StuppScope scope1 = StuppScope.newDefinition().addType(bookType).addType(authorType).createScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope1);
		StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(authorType, scope1);
		
		Book book = bookFactory.newInstance(1L);
		Author author = authorFactory.newInstance(2L);
		book.setAuthor(author);
		
		scope1.detach(book);
		assertNull(Stupp.getScope(book));
		assertNull(Stupp.getScope(author));
		
		final StuppScope scope2 = StuppScope.newDefinition()
			.addType(bookType)
			.addType(authorType)
			.createScope();
		scope2.attach(book);
		assertEquals(scope2, Stupp.getScope(book));
		assertEquals(scope2, Stupp.getScope(author));
	}

	public void testCollection() {
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppScope bookScope = StuppScope.newDefinition().addType(bookType).createScope();
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, bookScope);
		final StuppType pubType = StuppType.getInstance(Publisher.class);
		final StuppScope pubScope = StuppScope.newDefinition().addType(bookType).addType(pubType).createScope();
		StuppFactory<Publisher, Integer> pubFactory = new StuppFactory<Publisher, Integer>(pubType, pubScope);
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
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppType authorType = StuppType.getInstance(Author.class);
		final StuppScope scope = StuppScope.newDefinition().addType(bookType).addType(authorType).createScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope);
		final StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(authorType, scope);
		
		final Book book = bookFactory.newInstance(1L);
		final Author author = authorFactory.newInstance(1L);

		final StuppUniqueIndex bookIndex = (StuppUniqueIndex) scope.getPrimaryIndex(bookType);
		final StuppUniqueIndex authorIndex = (StuppUniqueIndex) scope.getPrimaryIndex(authorType);
		assertEquals(book, bookIndex.getSingle(bookIndex.getProperties().tupleFromValues(1L)));
		assertEquals(author, authorIndex.getSingle(authorIndex.getProperties().tupleFromValues(1L)));
		
	}
	
	public void testSameTypeSameKey() {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition().addType(type).createScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		final Book book = bookFactory.newInstance(1L);
		try {
			bookFactory.newInstance(1L);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
	}

	public void testChangeKey() {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition().addType(type).createScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
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
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition().addType(type).createScope();
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
		final StuppType bookType = StuppType.getInstance(Book.class);
		final StuppType authorType = StuppType.getInstance(Author.class);
		final StuppType pubType = StuppType.getInstance(Publisher.class);
		final StuppScope scope = StuppScope.newDefinition().addType(bookType).addType(authorType).addType(pubType).createScope();
		final StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(bookType, scope);
		final StuppFactory<Author, Long> authorFactory = new StuppFactory<Author, Long>(authorType, scope);
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
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition()
			.addType(type)
			.addIndex("test", type.properties("name"))
			.setIndexDefinition(StuppPropertyIndex.newDefinition("test"))
			.createScope();
		final StuppPropertyIndex index = (StuppPropertyIndex) scope.getIndex(type, "test");
		assertEquals(3, scope.getAllIndices().size());
		assertTrue(scope.getAllIndices().contains(index));
		assertEquals(1, scope.getIndices(type, "name").size());
		assertSame(index, scope.getIndices(type, "name").iterator().next());
	}

	public void testAllTypes() {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition()
			.addType(type)
			.createScope();
		Map<String, StuppType> map = scope.getAllTypes();
		assertEquals(1, map.size());
		assertEquals(type, map.get(type.getName()));
	}
	
	public void testDetachAll() {
		final StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition()
			.addType(type)
			.createScope();
		//check safe on totally empty
		scope.detachAll();
		assertEquals(0, scope.getAllObjects().size());
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
		//check index clear
		book = (Book) type.newInstance();
		book.setId(1L);
		scope.attach(book);
	}
	
	private static interface Jacket {
		
		@StuppIndexed
		void setBook(Book book);
		
	}
	
}
