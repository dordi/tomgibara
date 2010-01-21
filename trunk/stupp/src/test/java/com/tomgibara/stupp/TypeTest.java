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
import java.util.HashSet;

import com.tomgibara.stupp.StuppPropertyIndex.Definition;
import com.tomgibara.stupp.ann.StuppEquality;
import com.tomgibara.stupp.ann.StuppIndexed;
import com.tomgibara.stupp.ann.StuppNamed;

import junit.framework.TestCase;

public class TypeTest extends TestCase {

	public void testCanonicalDefinitions() {
		StuppType type1 = StuppType.newDefinition(Book.class).getType();
		StuppType type2 = StuppType.newDefinition(Book.class).getType();
		assertSame(type1, type2);
		StuppType type3 = StuppType.newDefinition(Author.class).getType();
		assertNotSame(type1, type3);
	}
	
	public void testDifferentDefinitions() {
		StuppType type1 = StuppType.newDefinition(Book.class).getType();
		StuppType type2 = StuppType.newDefinition(Book.class).setEqualityProperties("name").getType();
		StuppType type3 = StuppType.newDefinition(Book.class).addIndex("test", "name").getType();
		assertNotSame(type1, type2);
		assertNotSame(type2, type3);
		assertNotSame(type3, type1);
	}
	
	public void testModifiedDefinition() {
		StuppType.Definition def = StuppType.newDefinition(Book.class);
		StuppType type1 = def.getType();
		StuppType type2 = def.removeIndex("primary").addIndex("primary", "name").getType();
		assertFalse(type1.equals(type2));
	}
	
	public void testClonedDefinitions() {
		StuppType.Definition def1 = StuppType.newDefinition(Book.class);
		StuppType.Definition def2 = def1.clone().removeIndex("primary").addIndex("primary", "name");
		StuppType.Definition def3 = def2.clone().removeIndex("primary").addIndex("primary", "id");
		assertFalse(def1.equals(def2));
		assertFalse(def2.equals(def3));
		assertTrue(def3.equals(def1));
		StuppType type1 = def1.getType();
		StuppType type2 = def2.getType();
		StuppType type3 = def3.getType();
		assertFalse(type1.equals(type2));
		assertFalse(type2.equals(type3));
		assertTrue(type3.equals(type1));
	}
	
	public void testInstanceMethod() {
		StuppType type1 = StuppType.getInstance(Book.class);
		StuppType type2 = StuppType.newDefinition(Book.class).getType();
		assertSame(type1, type2);
	}

	public void testMultipleInterfaces() {
		StuppType baType = StuppType.newDefinition(Book.class, Catalogue.class).getType();
		Object instance = baType.newInstance();
		((Book) instance).setName("Book Property");
		((Catalogue) instance).setBooks(new HashSet<Book>());
	}
	
	public void testPresentKey() {
		StuppType.getInstance(A.class);
	}
	
	public void testMultipleKey() {
		StuppType.getInstance(B.class);
	}
	
	public void testOverrideKey() {
		StuppType type = StuppType.newDefinition(C.class).removeIndex("primary").addIndex("primary", "id").getType();
		C instance = (C) type.newInstance();
		type.getIndexProperties().tupleFromValues(1L).setOn(instance);
		assertEquals(1L, instance.getId());
	}

	public void testOverrideEquality() {
		StuppType type = StuppType.newDefinition(D.class).setEqualityProperties("forename", "surname").getType();
		D d1 = (D) type.newInstance();
		D d2 = (D) type.newInstance();
		assertEquals(d1, d2);
		assertEquals(d2, d1);
		d1.setSurname("Gibara");
		assertFalse(d1.equals(d2));
		assertFalse(d2.equals(d1));
		d2.setSurname("Gibara");
		assertEquals(d1, d2);
		assertEquals(d2, d1);
		d1.setId(1L);
		d2.setId(2L);
		assertEquals(d1, d2);
		assertEquals(d2, d1);
		d1.setForename("Tom");
		assertFalse(d1.equals(d2));
		assertFalse(d2.equals(d1));
	}

	public void testIncompatibleTypes() {
		try {
			StuppType.getInstance(E.class);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			StuppType.getInstance(F.class);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			StuppType.getInstance(G.class);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}

		StuppType.getInstance(H.class);

		
	}
	
	public void testAnnotatedIndexDefinition() {
		StuppType type = StuppType.getInstance(I.class);
		Collection<? extends StuppIndex<?>> indices = type.createIndices();
		assertEquals(1, indices.size());
		StuppPropertyIndex index = (StuppPropertyIndex) indices.iterator().next();
	}
	
	public void testIndexDefinition() {
		final Definition indexDefinition = StuppPropertyIndex.newDefinition(StuppType.PRIMARY_INDEX_NAME);
		final StuppType type = StuppType.newDefinition(A.class).setIndexDefinition(indexDefinition).getType();
		Collection<? extends StuppIndex<?>> indices = type.createIndices();
		assertEquals(1, indices.size());
		StuppPropertyIndex index = (StuppPropertyIndex) indices.iterator().next();
	}
	
	public void testTypeName() {
		final StuppType testType = StuppType.getInstance(Tést.class);
		StuppType.checkName(testType.getName());
		final StuppType teeTum = StuppType.newDefinition(Dee.class, Dum.class).getType();
		assertEquals("Tee_Tum", teeTum.getName());
		try {
			StuppType.getInstance(BadName.class);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	private static interface A {
		@StuppIndexed
		void setKey(String id);
	}
	
	private static interface B {
		@StuppIndexed
		void setKey(String key);
		@StuppIndexed
		void setId(long id);
	}
	
	private static interface C {
		@StuppIndexed
		void setKey(String key);
		void setId(long id);
		long getId();
	}
	
	private static interface D {
		
		@StuppIndexed
		void setId(long id);
		
		@StuppEquality
		void setForename(String forename);
		
		@StuppEquality
		void setSurname(String surname);
	}

	private static interface E {
		
		@StuppIndexed
		void setId(long id);

		void setValue(long v);
		
		int getValue();
	}
	
	private static interface F {
		
		@StuppIndexed
		void setId(long id);

		void setValue(String v);
		
		Boolean getValue();
	}
	
	private static interface G {
		
		@StuppIndexed
		void setId(long id);

		void setValue(Number v);
		
		Integer getValue();
	}
	
	private static interface H {
		
		@StuppIndexed
		void setId(long id);

		void setValue(Integer v);
		
		Number getValue();
	}
	
	@StuppPropertyIndex.Definition(name = "prop")
	private static interface I {
		
		@StuppIndexed(name = "prop")
		void setValue(String value);
		
		String getValue();
		
	}

	private static interface Tést {
		
	}
	
	@StuppNamed("Tee")
	private static interface Dee {
		
	}
	
	@StuppNamed("Tum")
	private static interface Dum {
		
	}
	
	@StuppNamed("****")
	private static interface BadName {
		
	}
}
