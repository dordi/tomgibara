package com.tomgibara.geo;

import junit.framework.TestCase;

public class OSGridTest extends TestCase {

	private static GridRefSystem system = GridRefSystem.OSGB36;
	private static OSGrid grid = (OSGrid) system.getGrid();

	private GridRef ref(int e, int n) {
		return new GridRef(system, e, n);
	}
	
	private GridRef ref(String str) {
		return grid.refFromString(system, str);
	}
	
	public void testBasic() {
		
		assertEquals(ref(216650, 771250), ref("NN166712"));
		assertEquals(ref(216650, 771250), ref("NN 166712"));
		assertEquals(ref(216650, 771250), ref("NN166 712"));
		assertEquals(ref(216650, 771250), ref("NN 166 712"));
		assertEquals(ref(216650, 771250), ref("NN   166   712"));
		
		ref("NN");
		ref("NN12");
		ref("NN1234");
		ref("NN123456");
		ref("NN12345678");
		ref("NN1234567890");
		
		assertBad("NN 1660 712");
		assertBad("NN 1234567 1234567");
		assertBad("AI 123 456");
		assertBad("IA 123 456");
		
		checkFormat("NN");
		checkFormat("NN17");
		checkFormat("NN1671");
		checkFormat("NN 166 712");
	}
	
	private void checkFormat(String str) {
		assertEquals(str, grid.refToString(ref(str)));
	}

	private void assertBad(String str) {
		try {
			fail("Gave: " + ref(str));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
}
