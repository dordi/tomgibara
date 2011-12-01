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

import java.util.HashMap;
import java.util.Map;

import com.tomgibara.crinch.collections.ParameterMap;

/**
 * Simple app for checking that the {@link ParameterMap} implementation is not grotesquely slower than the HashMap implementation.
 * Tests thus far indicate that performance is comparable for dense maps.
 * 
 * @author Tom
 *
 */

public class ParameterMapPerf {

	private static final int cycles = 4;
	private static final int repetitions = 1000000;
	private static final int elements = 100;
	
	public static void main(String[] args) {
		final String[] keys = new String[elements];
		keys[0] = "name"; keys[1] = "dob"; keys[2] = "email";
		for (int i = 3; i < elements; i++) keys[i] = Integer.toString(i);
		for (int i = 0; i < cycles; i++) testCycle(keys);
	}
	
	static void testCycle(String[] keys) {

		final ParameterMap.Constructor<Object> cons = ParameterMap.constructor(keys);

		MapSource mine = new MapSource() {
			@Override
			public Map<String, Object> newMap() {
				return cons.newMap();
			}
		};
		
		MapSource theirs = new MapSource() {
			@Override
			public Map<String, Object> newMap() {
				return new HashMap<String, Object>();
			}
		};
		
		long pt = testMap(mine);
		long ht = testMap(theirs);

		System.out.println(String.format("PARAM: %6d   HASH: %6d", pt, ht));
	}
	
	static long testMap(MapSource source) {
		System.gc();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < repetitions; i++) {
			playWithMap(source.newMap());
		}
		return System.currentTimeMillis() - start;
	}
	
	public static Object dummy = null;
	
	static void playWithMap(Map<String, Object> map) {
		map.put("name", "Tom");
		map.put("dob", null);
		map.put("email", "me@example.com");
		map.get("name");
		map.get("dob");
		map.get("email");
		map.containsKey("name");
		for (Object value : map.values()) dummy = value;
		map.remove("name");
		map.clear();
		map.size();
		if (map instanceof ParameterMap) {
			dummy = ((ParameterMap<Object>)map).clone();
		} else {
			dummy = ((HashMap<String, Object>)map).clone();
		}
	}
	
	private interface MapSource {
		
		Map<String, Object> newMap();
		
	}
	
}
