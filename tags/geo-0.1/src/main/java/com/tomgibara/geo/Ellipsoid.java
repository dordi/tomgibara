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

/**
 * An approximation of a planetary shape against which coordinates may be defined.
 * 
 * @author Tom Gibara
 */

public final class Ellipsoid {

	// used for OS
	public static final Ellipsoid AIRY_1830 = withAxes(6377563.396, 6356256.910);

	// used for OSI
	public static final Ellipsoid MODIFIED_AIRY = withAxes(6377340.189, 6356034.447);

	// used for Channel Islands
	public static final Ellipsoid INT_1924 = withAxes(6378388.000, 6356911.946);
	
	// used for GPS
	public static final Ellipsoid WGS84 = withAxes(6378137.000, 6356752.314);
	public static final Ellipsoid GRS80 = withAxes(6378137.000, 6356752.314); // TODO inverse flattening
	
	public static Ellipsoid withAxes(double major, double minor) {
		return canonical(new Ellipsoid(major, minor));
	}
	
	public final double a; // semi-major axis
	public final double b; // semi-minor axis
	public final double e2; // eccentricity
	
	final double n;
	final double n2;
	final double n3;
	
	private Ellipsoid(double a, double b) {
		if (!GeoUtil.isCoordinate(a)) throw new IllegalArgumentException("a invalid");
		if (!GeoUtil.isCoordinate(b)) throw new IllegalArgumentException("b invalid");
		if (a <= 0) throw new IllegalArgumentException("a not positive");
		if (b <= 0) throw new IllegalArgumentException("b not positive");
		if (a < b) throw new IllegalArgumentException("a not major");
		this.a = a;
		this.b = b;
		e2 = 1 - (b*b)/(a*a);
		n = (a-b)/(a+b);
		n2 = n * n;
		n3 = n2 * n;
	}
	
	@Override
	public int hashCode() {
		return GeoUtil.hashCode(a) ^ 31 * GeoUtil.hashCode(b);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Ellipsoid)) return false;
		Ellipsoid that = (Ellipsoid) obj;
		if (this.a != that.a) return false;
		if (this.b != that.b) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Equ. " + a +"m Pol. " + b + "m";
	}
	
}
