package com.tomgibara.geo;

import static java.lang.Math.toRadians;

public class Datum {

	public static final Datum OSGB36 = new Datum(
			Ellipsoid.AIRY_1830,
			0.9996012717,
			toRadians(49),
			toRadians(-2),
			400000,
			-100000
			);
	
	public static final Datum WSG84 = new Datum(
			Ellipsoid.WGS84,
			1,
			Math.toRadians(5.31/3600),
			0,
			0,
			0
			);
	
	public static final Datum OSI65 = new Datum(
			Ellipsoid.MODIFIED_AIRY,
			0.99999185,
			toRadians(53.5),
			toRadians(-8),
			200000,
			250000
			);
	
	public final Ellipsoid ellipsoid;
	
	public final double F0; // scale factor
	public final double lat0; // latitude of true origin (radians)
	public final double lon0; // longitude of true origin (radians)
	public final double E0; // eastings of true origin (metres)
	public final double N0; // northing of true origin (metres)
	
	public Datum(Ellipsoid ellipsoid, double F0, double lat0, double lon0, double E0, double N0) {
		if (ellipsoid == null) throw new IllegalArgumentException("null ellipsoid");
		//TODO check other parameters
		this.ellipsoid = ellipsoid;
		this.F0 = F0;
		this.lat0 = lat0;
		this.lon0 = lon0;
		this.E0 = E0;
		this.N0 = N0;
	}

	Cartesian latLonHeightToCartesian(LatLonHeight latLonHeight) {
		LatLon latLon = latLonHeight.getLatLon();
		double lat = Math.toRadians(latLon.getLatitude()), lon = Math.toRadians(latLon.getLongitude()), h = latLonHeight.getHeight();
		double e2 = ellipsoid.e2;
		double s = Math.sin(lat);
		double c = Math.cos(lat);
		double v = ellipsoid.a / (Math.sqrt (1 - e2 * s * s));
		double x = (v + h) * c * Math.cos(lon);
		double y = (v + h) * c * Math.sin(lon);
		double z = ((1 - e2) * v + h) * s;
		return new Cartesian(x, y, z);
	}

	LatLonHeight cartesianToLatLonHeight(Cartesian cartesian) {
		double x = cartesian.getX(), y = cartesian.getY(), z = cartesian.getZ();
		double e2 = ellipsoid.e2;
		double lon = Math.atan(y / x);
		double p = Math.sqrt((x * x) + (y * y));
		double lat = Math.atan(z / (p * (1 - e2)));
		double s = Math.sin(lat);
		double v = ellipsoid.a / (Math.sqrt(1 - e2 * s * s));
		do {
		    double lat0 = Math.atan((z + e2 * v * Math.sin(lat)) / p);  
		    if (Math.abs(lat0 - lat) < 0.00001) break;
		    lat = lat0;
		} while (true);
		double h = p / Math.cos(lat) - v;
		return new LatLonHeight(new LatLon(this, Math.toDegrees(lat), Math.toDegrees(lon)), h);
	}
	
}
