package com.tomgibara.geo;

import junit.framework.TestCase;

public class GridRefTest extends TestCase {

	GridRefSystem system = GridRefSystem.OSGB36;
	
	public void testBasic() throws Exception {
		LatLon ll = system.createGridRef("NN166712").toLatLon();
		assertAlmostEquals(56.796556, ll.getLatitude());
		assertAlmostEquals(-5.00393, ll.getLongitude());
	}
	
	void assertAlmostEquals(double a, double b) {
		assertTrue(Math.abs(a-b) < 0.01);
	}
	
}
