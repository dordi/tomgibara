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

public class StuppTest extends TestCase {

	public void testSetKey() {
		StuppType type = StuppType.getInstance(Book.class);
		Object instance = type.newInstance();
		type.getIndexProperties().tupleFromValues(1L).setOn(instance);
		assertEquals(((Book)instance).getId(), 1L);
	}
	
	public void testSetInvalidKey() {
		StuppType type = StuppType.getInstance(Book.class);
		Object instance = type.newInstance();
		try {
			type.getIndexProperties().tupleFromValues("1").setOn(instance);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
}
