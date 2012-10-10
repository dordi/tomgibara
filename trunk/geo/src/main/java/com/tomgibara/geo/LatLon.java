package com.tomgibara.geo;

import static com.tomgibara.geo.GeoUtil.angleInMinutes;

public final class LatLon {

	private final Datum datum;
	private final double latitude;
	private final double longitude;
	
	//TODO should normalize 
	public LatLon(Datum datum, double latitude, double longitude) {
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
