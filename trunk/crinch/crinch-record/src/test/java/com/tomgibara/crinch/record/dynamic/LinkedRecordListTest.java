package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.def.ColumnType.INT_PRIMITIVE;

import java.util.Arrays;

import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.def.RecordDefinition;

import junit.framework.TestCase;

public class LinkedRecordListTest extends TestCase {

	public void testBasics() {
		RecordDefinition def = RecordDefinition
			.fromTypes(Arrays.asList(INT_PRIMITIVE))
			.setOrdinal(false)
			.setPositional(false)
			.build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinkedRecord[] recs = new LinkedRecord[5];
		DynamicRecordFactory.ClassConfig config = new DynamicRecordFactory.ClassConfig(false, true);
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
