package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.CHAR_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.INT_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.LONG_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.STRING_OBJECT;

import java.util.ArrayList;
import java.util.Arrays;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ParsedRecord;
import com.tomgibara.crinch.record.SingletonRecord;
import com.tomgibara.crinch.record.StdColumnParser;
import com.tomgibara.crinch.record.StringRecord;
import com.tomgibara.crinch.record.ColumnParser;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDefinition;

import junit.framework.TestCase;

public class DynamicRecordFactoryTest extends TestCase {

	private static final ColumnParser parser = new StdColumnParser();
	
//	public void testDefinitionCons() {
//		try {
//			new RecordDefinition(true, true, null);
//			fail();
//		} catch (IllegalArgumentException e) {
//			/* expected */
//		}
//		
//		try {
//			new RecordDefinition(true, true, Arrays.asList((ColumnType) null), null);
//			fail();
//		} catch (IllegalArgumentException e) {
//			/* expected */
//		}
//		
//		try {
//			new RecordDefinition(true, true, Arrays.asList(INT_PRIMITIVE), Arrays.asList(new ColumnOrder(-1, true, true)));
//			fail();
//		} catch (IllegalArgumentException e) {
//			/* expected */
//		}
//		
//		try {
//			new RecordDefinition(true, true, Arrays.asList(INT_PRIMITIVE), Arrays.asList(new ColumnOrder(1, true, true)));
//			fail();
//		} catch (IllegalArgumentException e) {
//			/* expected */
//		}
//		
//		try {
//			new RecordDefinition(true, true, Arrays.asList(INT_PRIMITIVE), Arrays.asList(new ColumnOrder(0, true, true), new ColumnOrder(0, true, true)));
//			fail();
//		} catch (IllegalArgumentException e) {
//			/* expected */
//		}
//	}
	
	public void testGetName() {
		
		RecordDefinition def1 = RecordDefinition.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, STRING_OBJECT)).build().withOrdering(Arrays.asList(null, null, new ColumnOrder(0, false, false)));
		RecordDefinition def2 = RecordDefinition.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE)).build().withOrdering(Arrays.asList(null, null, new ColumnOrder(0, false, false)));
		RecordDefinition def3 = RecordDefinition.fromTypes(Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE)).build();
		
		DynamicRecordFactory fac1 = DynamicRecordFactory.getInstance(def1);
		DynamicRecordFactory fac2 = DynamicRecordFactory.getInstance(def2);
		DynamicRecordFactory fac3 = DynamicRecordFactory.getInstance(def3);
		
		assertFalse(fac1.getName().equals(fac2.getName()));
		assertFalse(fac2.getName().equals(fac3.getName()));
		assertFalse(fac3.getName().equals(fac1.getName()));
	}
	
	public void testNewRecord() {
		RecordDefinition def = RecordDefinition
				.fromTypes(Arrays.asList(INT_PRIMITIVE, BOOLEAN_PRIMITIVE, BOOLEAN_WRAPPER, STRING_OBJECT, LONG_PRIMITIVE, CHAR_WRAPPER))
				.setPositional(false)
				.build().withOrdering(Arrays.asList(new ColumnOrder(0, true, false), new ColumnOrder(1, true, false), new ColumnOrder(2, false, true), new ColumnOrder(3, true, false), new ColumnOrder(4, true, false)));
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec = fac.newRecord(new DynamicRecordFactory.ClassConfig(false, false), new ParsedRecord(parser, new StringRecord(0L, -1L, "1", "true", "", "Tom", "3847239847239843", "")));
		assertEquals("[1,true,null,Tom,3847239847239843,null]", rec.toString());
	}
	
	public void testNewRecordFromBasis() {
		RecordDefinition basis = RecordDefinition
				.fromTypes(Arrays.asList(INT_PRIMITIVE, BOOLEAN_PRIMITIVE, BOOLEAN_WRAPPER, STRING_OBJECT, LONG_PRIMITIVE, CHAR_WRAPPER))
				.setPositional(false)
				.build()
				.withOrdering(Arrays.asList(new ColumnOrder(0, true, false), new ColumnOrder(1, true, false), new ColumnOrder(2, false, true), new ColumnOrder(3, true, false), new ColumnOrder(4, true, false)))
				.asBasis();
		RecordDefinition def = basis.asBasisToBuild().select(5).add().select(3).add().select(1).add().build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec = fac.newRecord(new DynamicRecordFactory.ClassConfig(false, false), new ParsedRecord(parser, new StringRecord(0L, -1L, "1", "true", "", "Tom", "3847239847239843", "")), true);
		assertEquals("[null,Tom,true]", rec.toString());
	}
	
	public void testLinkedRecord() {
		RecordDefinition def = RecordDefinition
				.fromTypes(Arrays.asList(INT_PRIMITIVE))
				.setOrdinal(false)
				.setPositional(false)
				.build();
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinkedRecord[] recs = new LinkedRecord[5];
		for (int i = 0; i < recs.length; i++) {
			recs[i] = (LinkedRecord) fac.newRecord(new DynamicRecordFactory.ClassConfig(false, true), new SingletonRecord(i));
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
	
}
