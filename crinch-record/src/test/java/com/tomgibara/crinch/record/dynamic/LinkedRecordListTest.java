/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.def.ColumnType.INT_PRIMITIVE;

import java.util.Arrays;

import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.def.RecordDef;

import junit.framework.TestCase;

public class LinkedRecordListTest extends TestCase {

	public void testBasics() {
		RecordDef def = RecordDef
			.fromTypes(Arrays.asList(INT_PRIMITIVE))
			.setOrdinal(false)
			.setPositional(false)
			.build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinkedRecord[] recs = new LinkedRecord[5];
		DynamicRecordFactory.ClassConfig config = new DynamicRecordFactory.ClassConfig(false, true, false);
		for (int i = 0; i < recs.length; i++) {
			recs[i] = (LinkedRecord) fac.newRecord(config, new SingletonRecord(i));
		}
		
		LinkedRecordList<LinkedRecord> list = new LinkedRecordList<LinkedRecord>();
		assertTrue(list.isEmpty());
		
		list.add(recs[0]);
		list.add(recs[1]);
		list.add(recs[2]);
		assertEquals(3, list.size());
		assertEquals(Arrays.asList(recs[0], recs[1], recs[2]), list);
		
		list.remove(recs[0]);
		assertEquals(Arrays.asList(recs[1], recs[2]), list);
		
		list.remove(0);
		assertEquals(Arrays.asList(recs[2]), list);
		
		list.add(0, recs[1]);
		assertEquals(Arrays.asList(recs[1], recs[2]), list);
		
		list.clear();
		assertTrue(list.isEmpty());
		list.clear();
		assertTrue(list.isEmpty());

		list = new LinkedRecordList<LinkedRecord>(Arrays.asList(recs));
		assertEquals(Arrays.asList(recs), list);
	}
	
}
