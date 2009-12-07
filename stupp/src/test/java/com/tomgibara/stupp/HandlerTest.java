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

import com.tomgibara.stupp.Stupp;
import com.tomgibara.stupp.StuppFactory;
import com.tomgibara.stupp.StuppScope;
import com.tomgibara.stupp.StuppType;

import junit.framework.TestCase;

public class HandlerTest extends TestCase {

	public void testEqualProperties() throws Exception {
		StuppType type = StuppType.getInstance(Book.class);
		Book book1 = (Book) type.newInstance();
		Book book2 = (Book) type.newInstance();
		assertEquals(book1, book2);
		book1.setName("Perl Cookbook");
		assertFalse(book1.equals(book2));
		book2.setName("Perl Cookbook");
		assertEquals(book1, book2);
		Stupp.setKey(book1, 1L);
		Stupp.setKey(book2, 2L);
		assertFalse(book1.equals(book2));
		Stupp.setKey(book2, 1L);
		assertEquals(book1, book2);
	}

	public void testEqualSelf() {
		StuppType type = StuppType.getInstance(Book.class);
		Book book = (Book) type.newInstance();
		assertEquals(book, book);
	}
	
	public void testUnequalScopes() throws Exception {
		StuppFactory<Book, Long> factory1 = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), new StuppScope());
		StuppFactory<Book, Long> factory2 = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), new StuppScope());
		Book book1 = factory1.newInstance(0L);
		Book book2 = factory2.newInstance(0L);
		assertFalse(book1.equals(book2));
	}

	public void testUnequalTypes() {
		StuppType bookType = StuppType.getInstance(Book.class);
		StuppType authorType = StuppType.getInstance(Author.class);
		Object book = bookType.newInstance();
		Object author = authorType.newInstance();
		assertFalse(book.equals(author));
	}

	public void testToString() {
		StuppType bookType = StuppType.getInstance(Book.class);
		Object book = bookType.newInstance();
		book.toString();
	}
	
}
