package com.tomgibara.geo;

public final class LatLon {

	private final double latitude;
	private final double longitude;
	
	public LatLon(double latitude, double longitude) {
		//TODO tests worthwhile?
		if (!GeoUtil.isCoordinate(latitude)) throw new IllegalArgumentException("latitude not a coordinate");
		if (!GeoUtil.isCoordinate(longitude)) throw new IllegalArgumentException("longitude not a coordinate");
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	@Override
	public int hashCode() {
		return GeoUtil.hashCode(latitude) ^ 31 * GeoUtil.hashCode(longitude);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof LatLon)) return false;
		LatLon that = (LatLon) obj;
		if (this.latitude != that.latitude) return false;
		if (this.longitude != that.longitude) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%.6f\u00b0,%.6f\u00b0", latitude, longitude);
	}
	
}
