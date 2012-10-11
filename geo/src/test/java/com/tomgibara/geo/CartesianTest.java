package com.tomgibara.geo;


public class CartesianTest extends GeoTest {

	public void testRoundTrip() {
		LatLonHeight llh1 = GridRefSystem.OSGB36.createGridRef("NN166712").toLatLon().atHeight(0);
		LatLonHeight llh2 = llh1.toCartesian().toLatLonHeight(GridRefSystem.OSGB36.getDatum());
		assertClose(llh1, llh2);
	}
	
}
