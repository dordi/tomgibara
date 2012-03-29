package com.tomgibara.crinch.record.def;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tomgibara.crinch.record.def.ColumnOrder.Sort;

import junit.framework.TestCase;

public class RecordDefTest extends TestCase {

	private static List<ColumnType> types(ColumnType... types) {
		return Arrays.asList(types);
	}
	
	private static List<ColumnOrder> orders(ColumnOrder... orders) {
		return Arrays.asList(orders);
	}

	private static Map<String, String> props(String... props) {
		if ((props.length & 1) != 0) throw new IllegalArgumentException();
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < props.length; i += 2) {
			map.put(props[i], props[i+1]);
		}
		return map;
	}
	
	public void testTrailing() {
		try {
			RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}

		try {
			RecordDef.fromScratch().order(null).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		
		try {
			RecordDef.fromScratch().columnProp("a", "b").build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		
		try {
			RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).add().build().asBasisToBuild().select(0).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
	}
	
	public void testWithOrdering() {
		
		RecordDef def = RecordDef.fromTypes(types(ColumnType.INT_PRIMITIVE, ColumnType.BOOLEAN_PRIMITIVE)).build().withOrdering(orders(new ColumnOrder(0, Sort.ASCENDING, true), new ColumnOrder(1, Sort.DESCENDING, false))).asBasis();
		assertEquals(0, def.getColumns().get(0).getOrder().getPrecedence());
		assertEquals(1, def.getColumns().get(1).getOrder().getPrecedence());
		RecordDef sub = def.withOrdering(orders());
		assertNull(sub.getColumns().get(0).getOrder());
		assertNull(sub.getColumns().get(1).getOrder());
		
		sub = sub.withOrdering(orders(null, new ColumnOrder(0, Sort.ASCENDING, false)));
		assertNull(sub.getColumns().get(0).getOrder());
		assertEquals(0, sub.getColumns().get(1).getOrder().getPrecedence());
	}
	
	public void testColumnProperties() {
		RecordDef def = RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).columnProp("key1", "value1").columnProp("key2", "value2").add().build();
		Map<String, String> props;
		props = def.getColumns().get(0).getProperties();
		assertEquals(2, props.size());
		assertEquals("value1", props.get("key1"));
		assertEquals("value2", props.get("key2"));
		RecordDef sub = def.asBasisToBuild().select(0).columnProp("key3", "value3").columnProp("key2", null).add().build();
		props = sub.getColumns().get(0).getProperties();
		assertEquals(2, props.size());
		assertEquals("value1", props.get("key1"));
		assertEquals("value3", props.get("key3"));
	}
	
	public void testRecordProperties() {
		RecordDef def = RecordDef.fromScratch().build();
		assertEquals(props(), def.getProperties());
		
		RecordDef sub = def.asBasisToBuild().recordProp("key1", "value1").recordProp("key2", "value2").build();
		assertEquals(props("key1", "value1", "key2", "value2"), sub.getProperties());
		
		sub = sub.asBasisToBuild().recordProp("key3", "value3").recordProp("key2", null).build();
		assertEquals(props("key1", "value1", "key3", "value3"), sub.getProperties());
	}
	
	public void testWithProperties() {
		RecordDef def = RecordDef.fromScratch().build();
		assertEquals(props(), def.getProperties());
		
		RecordDef sub = def.withProperties(props("key1", "value1", "key2", "value2"));		
		assertEquals(props("key1", "value1", "key2", "value2"), sub.getProperties());
		
		sub = sub.withProperties(props("key3", "value3", "key2", null));
		assertEquals(props("key1", "value1", "key3", "value3"), sub.getProperties());
	}
	
}
