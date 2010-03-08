/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.tomgibara.crinch.collections.ParameterMap.Constructor;

import junit.framework.TestCase;

//TODO add full tests for derived views (entrySet, values, keySet)
public class ParameterMapTest extends TestCase {

	private Constructor<Object> cons;
	
	@Override
	protected void setUp() throws Exception {
		cons = ParameterMap.constructor(new String[] {"name", "dob", "email"});
	}
	
	public void testEmptyMap() {
		doEmptyTesting(cons.newMap());
	}

	public void testPutGet() {
		ParameterMap<Object> map = cons.newMap();
		map.put("name", "Tom");
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());
		assertEquals("Tom", map.get("name"));
		assertEquals("Tom", map.values().iterator().next());
		assertNull(map.get("dob"));
		assertNull(map.get("email"));
		assertNull(map.get("dummy"));
		assertEquals("name", map.keySet().iterator().next());
		doEqualityTest(map, Collections.singletonMap("name", (Object)"Tom"));
	}
	
	public void testClear() {
		ParameterMap<Object> map = cons.newMap();
		map.put("name", "Tom");
		map.clear();
		doEmptyTesting(map);
	}
	
	public void testPutAll() {
		ParameterMap<Object> map = cons.newMap();
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("name", "Tom");
		m.put("dob", null);
		map.putAll(m);
		doEqualityTest(map, m);
		
		map.clear();
		map.put("name", "Other");
		map.putAll(m);
		doEqualityTest(map, m);
	}
	
	public void testPutRemove() {
		ParameterMap<Object> map = cons.newMap();
		map.put("name", "Tom");
		Object value = map.remove("name");
		assertEquals("Tom", value);
		doEmptyTesting(map);
	}
	
	public void testContains() {
		ParameterMap<Object> map = cons.newMap();
		assertFalse(map.containsKey("name"));
		assertFalse(map.containsValue("Tom"));
		map.put("name", "Tom");
		assertTrue(map.containsKey("name"));
		assertTrue(map.containsValue("Tom"));
	}
	
	public void testClone() {
		ParameterMap<Object> map = cons.newMap();
		map.put("name", "Tom");
		map.put("dob", null);
		doEqualityTest(map, map.clone());
	}
	
	public void testIteratorRemove() {
		ParameterMap<Object> map = cons.newMap();
		map.put("name", "Tom");
		map.put("dob", null);
		ParameterMap<Object> m;
		m = map.clone();
		doIteratorRemoveTesting(m, m.values().iterator());
		m = map.clone();
		doIteratorRemoveTesting(m, m.keySet().iterator());
		m = map.clone();
		doIteratorRemoveTesting(m, m.entrySet().iterator());
	}
	
	private void doIteratorRemoveTesting(Map<String, Object> map, Iterator<?> iterator) {
		int count = map.size();
		while(iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			count --;
		}
		assertEquals(0, count);
		doEmptyTesting(map);
	}
	
	private void doEmptyTesting(Map<String, Object> map) {
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
		assertFalse(map.values().iterator().hasNext());
		assertFalse(map.keySet().iterator().hasNext());
		assertNull(map.get("name"));
		assertNull(map.get("dob"));
		assertNull(map.get("email"));
		assertNull(map.get("dummy"));
		Map<String, Object> m = Collections.emptyMap();
		doEqualityTest(map, m);
	}
	
	private void doEqualityTest(Map<String, Object> map, Map<String, Object> m) {
		assertEquals(m.hashCode(), map.hashCode());
		assertTrue(map.equals(m));
		assertTrue(map.entrySet().equals(m.entrySet()));
		assertTrue(map.keySet().equals(m.keySet()));
		assertEquals(new HashSet<Object>(map.values()), new HashSet<Object>(m.values()));
		assertTrue(m.equals(map));
		assertTrue(m.entrySet().equals(map.entrySet()));
		assertTrue(m.keySet().equals(map.keySet()));
	}
	
	public void todoTest() {

		
		//TODO move to test
		/*
		ParameterMap<Object> map = cons.newMap();
		
		System.out.println(map.isEmpty());
		map.put("name", "Tom");
		System.out.println(map.size() == 1);
		
		System.out.println(map);
		System.out.println(map.get("name"));
		System.out.println(map.get("dob"));
		System.out.println(map.size() == 3);
		
		map.put("dob", new Date());
		map.put("email", "tom@example.com");
		
		for (String key : map.keySet()) {
			System.out.println("KEY: " + key);
		}

		HashMap<String, Object> hmap = new HashMap<String, Object>(map);

		System.out.println(hmap);
		
		System.out.println(map.equals(hmap));
		
		System.out.println(map.toString());
		
		Object name = map.remove("name"); 
		System.out.println(name.equals("Tom"));
		
		ParameterMap<Object> map2 = cons.newMap(map);

		System.out.println(map.equals(map2));
		
		ParameterMap<Object> bigMap = ParameterMap.constructor(new String[] {"When", "I", "entered", "the", "egg", "and", "spoon", "race", "knew", "was", "not", "very", "quick"}).newMap();
		
		bigMap.put("When", 4);
		System.out.println(bigMap);
		*/
		
		/*
		for (int i = 0; i < 1000; i++) {
			HashSet<String> keys = new HashSet<String>();
			for (int j = 0; j < 10; j++) {
				keys.add( Double.toString( Math.random() ) );
			}
			System.out.println(keys);
			ParameterMap.constructor(keys);
		}
		*/
		

	}
}
