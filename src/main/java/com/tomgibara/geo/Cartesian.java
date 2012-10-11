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

/**
 * Cartesian coordinates of a point in 3D space.
 * 
 * @author Tom Gibara
 */

public class Cartesian {

	public static Cartesian inMeters(double x, double y, double z) {
		return new Cartesian(x,y,z);
	}
	
	private final double x;
	private final double y;
	private final double z;
	
	private Cartesian(double x, double y, double z) {
		if (!GeoUtil.isCoordinate(x)) throw new IllegalArgumentException("invalid x");
		if (!GeoUtil.isCoordinate(y)) throw new IllegalArgumentException("invalid y");
		if (!GeoUtil.isCoordinate(z)) throw new IllegalArgumentException("invalid z");
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public LatLonHeight toLatLonHeight(Datum datum) {
		if (datum == null) throw new IllegalArgumentException("null datum");
		return datum.cartesianToLatLonHeight(this);
	}
	
	@Override
	public int hashCode() {
		return GeoUtil.hashCode(x) + 31 * ( GeoUtil.hashCode(y) + 31 * (GeoUtil.hashCode(z)));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Cartesian)) return false;
		Cartesian that = (Cartesian) obj;
		if (this.x != that.x) return false;
		if (this.y != that.y) return false;
		if (this.z != that.z) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("[%.6f, %.6f, %.6f]", x, y, z);
	}
	
}
