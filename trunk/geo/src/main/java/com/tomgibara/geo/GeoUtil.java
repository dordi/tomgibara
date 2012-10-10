package com.tomgibara.geo;

public class GeoUtil {

	public static double arcMinsToRads(double arcSeconds) {
		return Math.toRadians(arcSeconds / 60);
	}

	public static double arcSecsToRads(double arcSeconds) {
		return Math.toRadians(arcSeconds / 3600);
	}

	public static String angleInMinutes(double angleInDegrees, Coordinate coordinate) {
		if (coordinate == Coordinate.ELEVATION) throw new IllegalArgumentException("elevation not angular");

		boolean neg = angleInDegrees < 0;
		char dir;
		switch (coordinate) {
		case LATITUDE:
			dir = neg ? 'W' : 'E';
			break;
		case LONGITUDE:
			dir = neg ? 'S' : 'N';
			break;
		default:
			throw new IllegalStateException("Coordinate not angular");
		}

		StringBuilder sb = new StringBuilder();
		angleInMinutes(neg ? -angleInDegrees : angleInDegrees, sb);
		return sb.append(dir).toString();
	}

	private static void angleInMinutes(double a, StringBuilder sb) {
		double deg = Math.floor(a);
		sb.append((long) deg).append('\u00b0');
		a = (a - deg) * 60;
		if (a < 1.0/60000) return;

		double min = Math.floor(a);
		sb.append((long) min).append('\'');
		a = (a - min) * 60;
		if (a < 1.0/1000) return;
		
		double sec = Math.floor(a);
		sb.append((long) sec);
		a = (a - sec) * 1000;
		
		long mil = (long) Math.round(a);
		if (mil != 0) sb.append(String.format("\u00b7%03d", mil));
		sb.append('"');
	}
	
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
