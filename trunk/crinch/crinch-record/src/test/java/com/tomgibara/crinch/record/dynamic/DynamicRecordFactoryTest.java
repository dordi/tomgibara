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

import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.CHAR_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.INT_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.INT_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.LONG_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.STRING_OBJECT;

import java.util.Arrays;

import com.tomgibara.crinch.hashing.CondensingWriteStream;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.record.ArrayRecord;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ParsedRecord;
import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.StdColumnParser;
import com.tomgibara.crinch.record.StringRecord;
import com.tomgibara.crinch.record.ColumnParser;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.ColumnOrder.Sort;

import junit.framework.TestCase;

public class DynamicRecordFactoryTest extends TestCase {

	private static final ColumnParser parser = new StdColumnParser();
	
	public void testGetName() {
		
		RecordDef def1 = RecordDef.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, STRING_OBJECT)).build().withOrdering(Arrays.asList(null, null, new ColumnOrder(0, Sort.DESCENDING, false)));
		RecordDef def2 = RecordDef.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE)).build().withOrdering(Arrays.asList(null, null, new ColumnOrder(0, Sort.DESCENDING, false)));
		RecordDef def3 = RecordDef.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE)).build();
		
		DynamicRecordFactory fac1 = DynamicRecordFactory.getInstance(def1);
		DynamicRecordFactory fac2 = DynamicRecordFactory.getInstance(def2);
		DynamicRecordFactory fac3 = DynamicRecordFactory.getInstance(def3);
		
		assertFalse(fac1.getName().equals(fac2.getName()));
		assertFalse(fac2.getName().equals(fac3.getName()));
		assertFalse(fac3.getName().equals(fac1.getName()));
	}
	
	public void testNewRecord() {
		RecordDef def = RecordDef
				.fromTypes(Arrays.asList(INT_PRIMITIVE, BOOLEAN_PRIMITIVE, BOOLEAN_WRAPPER, STRING_OBJECT, LONG_PRIMITIVE, CHAR_WRAPPER))
				.setPositional(false)
				.build().withOrdering(Arrays.asList(new ColumnOrder(0, Sort.ASCENDING, false), new ColumnOrder(1, Sort.ASCENDING, false), new ColumnOrder(2, Sort.DESCENDING, true), new ColumnOrder(3, Sort.ASCENDING, false), new ColumnOrder(4, Sort.ASCENDING, false)));
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec = fac.newRecord(new DynamicRecordFactory.ClassConfig(false, false, false), new ParsedRecord(parser, new StringRecord(0L, -1L, "1", "true", "", "Tom", "3847239847239843", "")));
		assertEquals("[1,true,null,Tom,3847239847239843,null]", rec.toString());
	}
	
	public void testNewRecordFromBasis() {
		RecordDef basis = RecordDef
				.fromTypes(Arrays.asList(INT_PRIMITIVE, BOOLEAN_PRIMITIVE, BOOLEAN_WRAPPER, STRING_OBJECT, LONG_PRIMITIVE, CHAR_WRAPPER))
				.setPositional(false)
				.build()
				.withOrdering(Arrays.asList(new ColumnOrder(0, Sort.ASCENDING, false), new ColumnOrder(1, Sort.ASCENDING, false), new ColumnOrder(2, Sort.DESCENDING, true), new ColumnOrder(3, Sort.ASCENDING, false), new ColumnOrder(4, Sort.ASCENDING, false)))
				.asBasis();
		RecordDef def = basis.asBasisToBuild().select(5).add().select(3).add().select(1).add().build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec = fac.newRecord(new DynamicRecordFactory.ClassConfig(false, false, false), new ParsedRecord(parser, new StringRecord(0L, -1L, "1", "true", "", "Tom", "3847239847239843", "")), true);
		assertEquals("[null,Tom,true]", rec.toString());
	}
	
	public void testWasNull() {
		for (DynamicRecordFactory.ClassConfig config : DynamicRecordFactory.ClassConfig.sInstances) {
			testWasNull(config);
		}
	}

	private void testWasNull(DynamicRecordFactory.ClassConfig config) {
		RecordDef def = RecordDef
				.fromTypes(Arrays.asList(STRING_OBJECT, STRING_OBJECT))
				.setPositional(false)
				.build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec1 = fac.newRecord(config, new ParsedRecord(parser, new StringRecord(0L, -1L, "SOMESTR", null)));
		assertEquals("SOMESTR", rec1.nextString());
		System.out.println(config);
		assertFalse(rec1.wasNull());
		assertNull(rec1.nextString());
		assertTrue(rec1.wasNull());
		LinearRecord rec2 = fac.newRecord(config, new ParsedRecord(parser, new StringRecord(0L, -1L, null, "SOMESTR")));
		assertNull(rec2.nextString());
		assertTrue(rec2.wasNull());
		assertEquals("SOMESTR", rec2.nextString());
		assertFalse(rec2.wasNull());
	}

	
	public void testLinkedRecord() {
		RecordDef def = RecordDef
				.fromTypes(Arrays.asList(INT_PRIMITIVE))
				.setOrdinal(false)
				.setPositional(false)
				.build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinkedRecord[] recs = new LinkedRecord[5];
		for (int i = 0; i < recs.length; i++) {
			recs[i] = (LinkedRecord) fac.newRecord(new DynamicRecordFactory.ClassConfig(false, true, false), new SingletonRecord(i));
		}
		recs[0].insertRecordBefore(recs[1]);
		assertEquals(recs[0], recs[1].getPreviousRecord());
		assertEquals(recs[1], recs[0].getNextRecord());
		recs[2].insertRecordAfter(recs[1]);
		assertEquals(recs[0], recs[1].getPreviousRecord());
		assertEquals(recs[1], recs[2].getPreviousRecord());
		assertEquals(recs[2], recs[0].getPreviousRecord());
		assertEquals(recs[1], recs[0].getNextRecord());
		assertEquals(recs[2], recs[1].getNextRecord());
		assertEquals(recs[0], recs[2].getNextRecord());
		recs[0].removeRecord();
		assertEquals(recs[0], recs[0].getNextRecord());
		assertEquals(recs[0], recs[0].getPreviousRecord());
		assertEquals(recs[1], recs[2].getPreviousRecord());
		assertEquals(recs[2], recs[1].getPreviousRecord());
		assertEquals(recs[1], recs[2].getNextRecord());
		assertEquals(recs[2], recs[1].getNextRecord());
	}

	public void testExtendedRecord() {
		testExtendedRecord(new DynamicRecordFactory.ClassConfig(false, false, true));
		testExtendedRecord(new DynamicRecordFactory.ClassConfig(true, false, true));
		testExtendedRecord(new DynamicRecordFactory.ClassConfig(false, true, true));
		testExtendedRecord(new DynamicRecordFactory.ClassConfig(true, true, true));
	}
	
	private void testExtendedRecord(DynamicRecordFactory.ClassConfig config) {
		RecordDef def = RecordDef
				.fromTypes(Arrays.asList(INT_PRIMITIVE))
				.setOrdinal(false)
				.setPositional(false)
				.build();
		
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		
		{
			Extended ext = (Extended) fac.newRecord(config, new SingletonRecord(0));
			assertNull(ext.getExtension());
			ext.setExtension("str");
			assertEquals("str", ext.getExtension());
			ext.setExtension(null);
			assertNull(ext.getExtension());
		}
	}

	public void testHashSource() {
		testHashSource(new DynamicRecordFactory.ClassConfig(false, false, false));
		testHashSource(new DynamicRecordFactory.ClassConfig(true, false, false));
		testHashSource(new DynamicRecordFactory.ClassConfig(false, true, false));
		testHashSource(new DynamicRecordFactory.ClassConfig(true, true, false));
	}

	public void testHashSource(DynamicRecordFactory.ClassConfig config) {
		RecordDef def = RecordDef
				.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_WRAPPER, STRING_OBJECT))
				.setOrdinal(false)
				.setPositional(false)
				.build();
		
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		HashSource<LinearRecord> src = fac.getHashSource(config);
		CondensingWriteStream out = new CondensingWriteStream();
		src.sourceData(fac.newRecord(config, new ArrayRecord(0L, 0L, new Object[]{ 1, null, null })), out);
		src.sourceData(fac.newRecord(config, new ArrayRecord(0L, 0L, new Object[]{ 1, null, "STR" })), out);
		src.sourceData(fac.newRecord(config, new ArrayRecord(0L, 0L, new Object[]{ 1, 10, null })), out);
		src.sourceData(fac.newRecord(config, new ArrayRecord(0L, 0L, new Object[]{ 1, 20, "STRING" })), out);
	}

}
