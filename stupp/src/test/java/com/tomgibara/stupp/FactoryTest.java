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

import java.util.Collections;

import com.tomgibara.stupp.StuppFactory;
import com.tomgibara.stupp.StuppScope;
import com.tomgibara.stupp.StuppType;

import junit.framework.TestCase;

//TODO must test types with subclassable keys
public class FactoryTest extends TestCase {

	public void testDuplicateNewInstance() {
		
		StuppFactory<Book, Long> factory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), new StuppScope());
		Book book = factory.newInstance(1L);
		try {
			factory.newInstance(1L);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	public void testDeleteInstance() {

		StuppFactory<Book, Long> factory = new StuppFactory<Book, Long>(StuppType.getInstance(Book.class), new StuppScope());
		Book book = factory.newInstance(1L);
		factory.deleteInstance(book);

		assertEquals(0, factory.getAllInstances().size());
		assertFalse(factory.deleteInstance(book));
	}

}
