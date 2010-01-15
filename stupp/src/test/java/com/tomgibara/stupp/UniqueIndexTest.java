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

import junit.framework.TestCase;

import com.tomgibara.stupp.StuppUniqueIndex.Definition;

public class UniqueIndexTest extends TestCase {

	public void testAnnotationConstruction() {
		final StuppType type = StuppType.getInstance(Book.class);
		final Definition definition = StuppUniqueIndex.newDefinition("test", false);
		final StuppUniqueIndex index = new StuppUniqueIndex(type.properties("name"), definition);
		assertEquals("test", index.getName());
		assertEquals(false, index.isNotNull());
	}
	
	public void testUniqueNullable() {
		
		StuppType type = StuppType.getInstance(Book.class);
		final StuppScope scope = StuppScope.newDefinition()
			.addType(type)
			.addIndex("test", type.properties("name"))
			.setIndexDefinition(StuppUniqueIndex.newDefinition("test", false))
			.createScope();
		final StuppUniqueIndex index = (StuppUniqueIndex) scope.getIndex(type, "test");

		StuppFactory<Book, Long> bookFactory = new StuppFactory<Book, Long>(type, scope);
		Book book1 = bookFactory.newInstance(1L);
		book1.setName("Tom");
		assertSame(book1, index.getSingle("Tom"));
		assertNull(index.getSingle("Jack"));
		book1.setName("Jack");
		assertSame(book1, index.getSingle("Jack"));
		Book book2 = bookFactory.newInstance(2L);
		try {
			book2.setName("Jack");
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		book2.setName("Tom");
		assertSame(book2, index.getSingle("Tom"));
	}
	
}
