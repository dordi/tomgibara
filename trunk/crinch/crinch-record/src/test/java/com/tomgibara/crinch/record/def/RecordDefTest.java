package com.tomgibara.crinch.record.def;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class RecordDefTest extends TestCase {

	private static List<ColumnType> types(ColumnType... types) {
		return Arrays.asList(types);
	}
	
	private static List<ColumnOrder> orders(ColumnOrder... orders) {
		return Arrays.asList(orders);
	}
	
	public void testWithOrdering() {
		
		RecordDef def = RecordDef.fromTypes(types(ColumnType.INT_PRIMITIVE, ColumnType.BOOLEAN_PRIMITIVE)).build().withOrdering(orders(new ColumnOrder(0, true, true), new ColumnOrder(1, false, false))).asBasis();
		assertEquals(0, def.getColumns().get(0).getOrder().getPrecedence());
		assertEquals(1, def.getColumns().get(1).getOrder().getPrecedence());
		RecordDef sub = def.withOrdering(orders());
		assertNull(sub.getColumns().get(0).getOrder());
		assertNull(sub.getColumns().get(1).getOrder());
		
		sub = sub.withOrdering(orders(null, new ColumnOrder(0, true, false)));
		assertNull(sub.getColumns().get(0).getOrder());
		assertEquals(0, sub.getColumns().get(1).getOrder().getPrecedence());
	}
	
}
