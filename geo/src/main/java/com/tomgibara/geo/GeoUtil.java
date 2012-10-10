package com.tomgibara.geo;

public class GeoUtil {

	static int hashCode(long value) {
		return (int)(value ^ (value >>> 32));
	}
	
	static int hashCode(double value) {
		return hashCode(Double.doubleToLongBits(value));
	}
	
	static boolean isCoordinate(double coord) {
		return !Double.isInfinite(coord) && !Double.isNaN(coord);
	}
	
}
