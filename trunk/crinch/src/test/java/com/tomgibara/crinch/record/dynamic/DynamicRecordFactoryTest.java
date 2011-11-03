package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.ColumnType.BOOLEAN_WRAPPER;
import static com.tomgibara.crinch.record.ColumnType.CHAR_WRAPPER;
import static com.tomgibara.crinch.record.ColumnType.INT_PRIMITIVE;
import static com.tomgibara.crinch.record.ColumnType.LONG_PRIMITIVE;
import static com.tomgibara.crinch.record.ColumnType.STRING_OBJECT;

import java.util.ArrayList;
import java.util.Arrays;

import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ParsedRecord;
import com.tomgibara.crinch.record.StdColumnParser;
import com.tomgibara.crinch.record.StringRecord;
import com.tomgibara.crinch.record.ColumnParser;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.Definition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.Order;

import junit.framework.TestCase;

public class DynamicRecordFactoryTest extends TestCase {

	private static final ColumnParser parser = new StdColumnParser();
	
	public void testDefinitionCons() {
		try {
			new Definition(true, true, null);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			new Definition(true, true, new ArrayList<ColumnType>(), (Order[]) null);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}

		try {
			new Definition(true, true, Arrays.asList((ColumnType) null));
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			new Definition(true, true, Arrays.asList(INT_PRIMITIVE), new Order(-1, true, true));
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			new Definition(true, true, Arrays.asList(INT_PRIMITIVE), new Order(1, true, true));
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		
		try {
			new Definition(true, true, Arrays.asList(INT_PRIMITIVE), new Order(0, true, true), new Order(0, true, true));
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
	public void testGetName() {
		
		Definition def1 = new Definition(true, true, Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, STRING_OBJECT), new Order(2, false, false));
		Definition def2 = new Definition(true, true, Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE), new Order(2, false, false));
		Definition def3 = new Definition(true, true, Arrays.asList(INT_PRIMITIVE, INT_PRIMITIVE, INT_PRIMITIVE));
		
		DynamicRecordFactory fac1 = DynamicRecordFactory.getInstance(def1);
		DynamicRecordFactory fac2 = DynamicRecordFactory.getInstance(def2);
		DynamicRecordFactory fac3 = DynamicRecordFactory.getInstance(def3);
		
		assertFalse(fac1.getName().equals(fac2.getName()));
		assertFalse(fac2.getName().equals(fac3.getName()));
		assertFalse(fac3.getName().equals(fac1.getName()));
	}
	
	public void testNewRecord() {
		Definition def = new Definition(true, false, Arrays.asList(INT_PRIMITIVE, BOOLEAN_PRIMITIVE, BOOLEAN_WRAPPER, STRING_OBJECT, LONG_PRIMITIVE, CHAR_WRAPPER), new Order(0, true, false), new Order(1, true, false), new Order(2, false, true), new Order(3, true, false), new Order(4, true, false));
		DynamicRecordFactory fac = DynamicRecordFactory.getInstance(def);
		LinearRecord rec = fac.newRecord(new ParsedRecord(parser, new StringRecord(0L, -1L, "1", "true", "", "Tom", "3847239847239843", "")));
		assertEquals("[1,true,null,Tom,3847239847239843,null]", rec.toString());
	}
	
}
