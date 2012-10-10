package com.tomgibara.geo;

public class Cartesian {

	private final double x;
	private final double y;
	private final double z;
	
	public Cartesian(double x, double y, double z) {
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
