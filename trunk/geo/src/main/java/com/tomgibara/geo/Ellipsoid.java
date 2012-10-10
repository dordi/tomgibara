package com.tomgibara.geo;

public final class Ellipsoid {

	// used for OS
	public static final Ellipsoid AIRY_1830 = new Ellipsoid(6377563.396, 6356256.910);

	// used for OSI
	public static final Ellipsoid MODIFIED_AIRY = new Ellipsoid(6377340.189, 6356034.447);

	// used for Channel Islands
	public static final Ellipsoid INT_1924 = new Ellipsoid(6378388.000, 6356911.946);
	
	// used for GPS
	public static final Ellipsoid WGS84 = new Ellipsoid(6378137.000, 6356752.314);
	public static final Ellipsoid GRS80 = new Ellipsoid(6378137.000, 6356752.314); // TODO inverse flattening
	
	public final double a; // semi-major axis
	public final double b; // semi-minor axis
	public final double e2; // eccentricity
	
	final double n;
	final double n2;
	final double n3;
	
	public Ellipsoid(double a, double b) {
		if (!GeoUtil.isCoordinate(a)) throw new IllegalArgumentException("a invalid");
		if (!GeoUtil.isCoordinate(b)) throw new IllegalArgumentException("b invalid");
		if (a <= 0) throw new IllegalArgumentException("a not positive");
		if (b <= 0) throw new IllegalArgumentException("b not positive");
		this.a = a;
		this.b = b;
		e2 = 1 - (b*b)/(a*a);
		n = (a-b)/(a+b);
		n2 = n * n;
		n3 = n2 * n;
	}
	
}
