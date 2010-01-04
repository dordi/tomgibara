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
