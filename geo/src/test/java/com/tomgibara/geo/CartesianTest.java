package com.tomgibara.geo;

import junit.framework.TestCase;

public class CartesianTest extends TestCase {

	private void assertClose(LatLonHeight llh1, LatLonHeight llh2) {
		LatLon ll1 = llh1.getLatLon();
		LatLon ll2 = llh2.getLatLon();
		double x = ll1.getLatitude() - ll2.getLatitude();
		double y = ll1.getLongitude() - ll2.getLongitude();
		assertTrue(x*x + y*y < 0.00001);
	}
	
	public void testRoundTrip() {
		LatLonHeight llh1 = GridRefSystem.OSGB36.createGridRef("NN166712").toLatLon().atHeight(0);
		LatLonHeight llh2 = llh1.toCartesian().toLatLonHeight(GridRefSystem.OSGB36.getDatum());
		assertClose(llh1, llh2);
	}
	
}
