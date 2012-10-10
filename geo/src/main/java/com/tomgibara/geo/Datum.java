package com.tomgibara.geo;

public class Datum {

	public static final Datum OSGB36 = new Datum(
			Ellipsoid.AIRY_1830,
			0.9996012717,
			Math.toRadians(49),
			Math.toRadians(-2)
			);
	
	public final Ellipsoid ellipsoid;
	
	public final double F0; // scale factor
	public final double lat0; // latitude of true origin (radians)
	public final double lon0; // longitude of true origin (radians)
	
	public Datum(Ellipsoid ellipsoid, double F0, double lat0, double lon0) {
		if (ellipsoid == null) throw new IllegalArgumentException("null ellipsoid");
		//TODO check other parameters
		this.ellipsoid = ellipsoid;
		this.F0 = F0;
		this.lat0 = lat0;
		this.lon0 = lon0;
	}

}
