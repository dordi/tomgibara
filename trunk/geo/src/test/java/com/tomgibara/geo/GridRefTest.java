package com.tomgibara.geo;

import junit.framework.TestCase;

public class GridRefTest extends GeoTest {

	public void testBasic() {
		LatLon ll = GridRefSystem.OSGB36.createGridRef("NN166712").toLatLon();
		assertAlmostEquals(56.796556, ll.getLatitude());
		assertAlmostEquals(-5.00393, ll.getLongitude());
	}
	
	public void testRoundTrip() {
		testRoundTrip(GridRefSystem.OSGB36.createGridRef("NN166712"));
		testRoundTrip(GridRefSystem.OSI65.createGridRef("H 79972 62472"));
	}
	
	private void testRoundTrip(GridRef ref) {
		GridRef ref2 = ref.toLatLon().toGridRef(ref.getSystem().getGrid());
		assertClose(ref, ref2);
	}
	
}
