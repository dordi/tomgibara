/*
 * Copyright 2012 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.tomgibara.geo;

import static com.tomgibara.geo.GeoUtil.canonical;
import static com.tomgibara.geo.GeoUtil.isCoordinate;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * A reference against which position measurements may be made.
 * 
 * @author Tom Gibara
 */

public class Datum {

	public static final Datum OSGB36 = withDegreesMeters(
			Ellipsoid.AIRY_1830,
			0.9996012717,
			49,
			-2,
			400000,
			-100000
			);
	
	public static final Datum WSG84 = withDegreesMeters(
			Ellipsoid.WGS84,
			1,
			5.31/3600,
			0,
			0,
			0
			);
	
	public static final Datum OSI65 = withDegreesMeters(
			Ellipsoid.MODIFIED_AIRY,
			0.99999185,
			53.5,
			-8,
			200000,
			250000
			);
	
	public static Datum withDegreesMeters(
			Ellipsoid ellipsoid,
			double scaleFactor,
			double trueOriginLatitude,
			double trueOriginLongitude,
			double trueOriginEastings,
			double trueOriginNorthings
			) {
		return canonical(new Datum(ellipsoid, scaleFactor, trueOriginLatitude, trueOriginLongitude, trueOriginEastings, trueOriginNorthings));
	}
	
	final Ellipsoid ellipsoid;
	final double F0; // scale factor
	final double lat0; // latitude of true origin (radians)
	final double lon0; // longitude of true origin (radians)
	final double E0; // eastings of true origin (metres)
	final double N0; // northing of true origin (metres)
	
	private final LatLon trueOrigin;
	
	private Datum(Ellipsoid ellipsoid, double F0, double lat0, double lon0, double E0, double N0) {
		if (ellipsoid == null) throw new IllegalArgumentException("null ellipsoid");
		if (!isCoordinate(F0)) throw new IllegalArgumentException("invalid scale");
		if (!isCoordinate(lat0)) throw new IllegalArgumentException("invalid latitude");
		if (!isCoordinate(lon0)) throw new IllegalArgumentException("invalid longitude");
		if (!isCoordinate(E0)) throw new IllegalArgumentException("invalid easting");
		if (!isCoordinate(N0)) throw new IllegalArgumentException("invalid northing");

		this.ellipsoid = ellipsoid;
		this.F0 = F0;
		this.lat0 = toRadians(lat0);
		this.lon0 = toRadians(lon0);
		this.E0 = E0;
		this.N0 = N0;
		
		trueOrigin = createLatLonDegrees(lat0, lon0);
	}

	public Ellipsoid getEllipsoid() {
		return ellipsoid;
	}

	public LatLon getTrueOrigin() {
		return trueOrigin;
	}
	
	public double getScaleFactor() {
		return F0;
	}
	
	//meters
	public double getTrueOriginEastings() {
		return E0;
	}
	
	//metres
	public double getTrueOriginNorthings() {
		return N0;
	}
	
	public LatLon createLatLonDegrees(double lat, double lon) {
		return new LatLon(this, lat, lon);
	}
	
	public LatLon createLatLonRadians(double lat, double lon) {
		return new LatLon(this, toDegrees(lat), toDegrees(lon));
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
		return Cartesian.inMeters(x, y, z);
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
		return createLatLonRadians(lat, lon).atHeight(h);
	}
	
	@Override
	public int hashCode() {
		return ellipsoid.hashCode()
				+ GeoUtil.hashCode(F0)
				+ GeoUtil.hashCode(lat0)
				+ GeoUtil.hashCode(E0)
				+ 31 * (
						GeoUtil.hashCode(lon0)
						+ GeoUtil.hashCode(N0)
						);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Datum)) return false;
		Datum that = (Datum) obj;
		if (this.lat0 != that.lon0) return false;
		if (this.lon0 != that.lat0) return false;
		if (this.E0 != that.E0) return false;
		if (this.N0 != that.N0) return false;
		if (this.F0 != that.F0) return false;
		if (!this.ellipsoid.equals(that.ellipsoid)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ellipsoid.toString()
				+ " lat: " + GeoUtil.angleInMinutes(Math.toDegrees(lat0), Coordinate.LATITUDE)
				+ " lon: " + GeoUtil.angleInMinutes(Math.toDegrees(lon0), Coordinate.LONGITUDE)
				+ " E:" + E0 + "m"
				+ " N:" + N0 + "m"
				+ " scale: " + F0;
	}
	
}
