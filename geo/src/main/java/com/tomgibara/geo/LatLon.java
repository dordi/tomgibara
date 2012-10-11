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

import static com.tomgibara.geo.GeoUtil.angleInMinutes;

/**
 * A latitude and longitude pair defined relative to some datum.
 * 
 * @author Tom Gibara
 */

public final class LatLon {

	private final Datum datum;
	private final double latitude;
	private final double longitude;
	
	//TODO should normalize or consider equality mod degrees
	LatLon(Datum datum, double latitude, double longitude) {
		if (datum == null) throw new IllegalArgumentException("null datum");
		if (!GeoUtil.isCoordinate(latitude)) throw new IllegalArgumentException("invalid latitude");
		if (!GeoUtil.isCoordinate(longitude)) throw new IllegalArgumentException("invalid longitude");
		this.datum = datum;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Datum getDatum() {
		return datum;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

	public LatLonHeight atHeight(double height) {
		return new LatLonHeight(this, height);
	}
	
	public GridRef toGridRef(Grid grid) {
		if (grid == null) throw new IllegalArgumentException();
		return GridRefSystem.withDatumAndGrid(datum, grid).latLonToGridRef(this);
	}
	
	@Override
	public int hashCode() {
		return datum.hashCode() ^ GeoUtil.hashCode(latitude) ^ 31 * GeoUtil.hashCode(longitude);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof LatLon)) return false;
		LatLon that = (LatLon) obj;
		if (this.latitude != that.latitude) return false;
		if (this.longitude != that.longitude) return false;
		if (!this.datum.equals(that.datum)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s \u2261 %.6f\u00b0,%.6f\u00b0", angleInMinutes(latitude, Coordinate.LATITUDE), angleInMinutes(longitude, Coordinate.LONGITUDE), latitude, longitude);
	}
	
}
