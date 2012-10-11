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

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for handling geographic data.
 * 
 * @author Tom Gibara
 *
 */

//TODO add naming
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
			dir = neg ? 'S' : 'N';
			break;
		case LONGITUDE:
			dir = neg ? 'W' : 'E';
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
	
	static <A> A canonical(A a) {
		return canon.get(a);
	}
	
	private static final Canon canon = new Canon();
	
	private static class Canon {

		private final ThreadLocal<Map<Object, Object>> local = new ThreadLocal<Map<Object,Object>>();
		private final Map<Object, Object> globalMap = new HashMap<Object, Object>();
		
		Canon() {
			local.set(new HashMap<Object, Object>());
		}
		
		@SuppressWarnings("unchecked")
		<A> A get(A a) {
			Map<Object, Object> localMap = local.get();
			Object obj = localMap.get(a);
			if (obj != null) return (A) obj;
			synchronized (globalMap) {
				obj = globalMap.get(a);
				if (obj == null) {
					globalMap.put(a, a);
					obj = a;
				}
			}
			localMap.put(obj, obj);
			return (A) obj;
		}
		
	}

	
}
