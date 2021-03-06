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

import java.util.Collection;

import com.tomgibara.stupp.StuppPropertyIndex.Definition;

import junit.framework.TestCase;

public class PropertyIndexTest extends TestCase {

	public void testAnnotationConstructor() {
		final StuppType type = StuppType.getInstance(Book.class);
		Definition definition = StuppPropertyIndex.newDefinition("test");
		final StuppPropertyIndex index = new StuppPropertyIndex(type.properties("name"), definition);
		assertEquals("test", index.getName());
	}
	
	public void testProperty() {
		StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope
			.newDefinition()
			.addType(type)
			.addIndex("test", type.properties("name"))
			.setIndexDefinition(StuppPropertyIndex.newDefinition("test"))
			.createScope();
		final StuppPropertyIndex index = (StuppPropertyIndex) scope.getIndex(type, "test");
		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		Book book1 = bookFactory.newInstance(1L);
		book1.setName("Tom");
		assertSame(book1, index.getSingle("Tom"));
		assertNull(index.getSingle("Jack"));
		book1.setName("Jack");
		assertSame(book1, index.getSingle("Jack"));
		Book book2 = bookFactory.newInstance(2L);
		book2.setName("Jack");
		Collection<Object> books = index.get("Jack");
		assertTrue(books.contains(book1));
		assertEquals(2, books.size());
	}

}
