package com.tomgibara.geo;

import java.util.Arrays;

import junit.framework.TestCase;

public class OSGridTest extends TestCase {

	private static OSGrid grid = OSGrid.instance;

	private static void out(int[] ings) {
		System.out.println( Arrays.toString(ings) );
	}
	
	private void assertEquals(int e, int n, int[] ings) {
		assertTrue(Arrays.equals(new int[] {e, n}, ings));
	}
	
	public void testBasic() {
		
		assertEquals(216650, 771250, grid.refFromString("NN166712"));
		assertEquals(216650, 771250, grid.refFromString("NN 166712"));
		assertEquals(216650, 771250, grid.refFromString("NN166 712"));
		assertEquals(216650, 771250, grid.refFromString("NN 166 712"));
		assertEquals(216650, 771250, grid.refFromString("NN   166   712"));
		
		grid.refFromString("NN");
		grid.refFromString("NN12");
		grid.refFromString("NN1234");
		grid.refFromString("NN123456");
		grid.refFromString("NN12345678");
		grid.refFromString("NN1234567890");
		
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
		int[] ref = grid.refFromString(str);
		assertEquals(str, grid.refToString(ref[0], ref[1]));
	}

	private void assertBad(String str) {
		try {
			fail("Gave: " + Arrays.toString(grid.refFromString(str)));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}
	
}
