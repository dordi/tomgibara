package com.tomgibara.geo;

public final class LatLonHeight {

	private final LatLon latLon;
	private final double height;
	
	public LatLonHeight(LatLon latLon, double height) {
		if (latLon == null) throw new IllegalArgumentException("null latLon");
		if (!GeoUtil.isCoordinate(height)) throw new IllegalArgumentException("invalid height");
		this.latLon = latLon;
		this.height = height;
	}
	
	public LatLon getLatLon() {
		return latLon;
	}
	
	public double getHeight() {
		return height;
	}
	
	public Cartesian toCartesian() {
		return latLon.getDatum().latLonHeightToCartesian(this);
	}

	@Override
	public int hashCode() {
		return latLon.hashCode() ^ GeoUtil.hashCode(height);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof LatLonHeight)) return false;
		LatLonHeight that = (LatLonHeight) obj;
		if (this.height != that.height) return false;
		if (!this.latLon.equals(that.latLon)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%.6f\u00b0,%.6f\u00b0,%.6fm", latLon.getLatitude(), latLon.getLongitude(), height);
	}
	
}
