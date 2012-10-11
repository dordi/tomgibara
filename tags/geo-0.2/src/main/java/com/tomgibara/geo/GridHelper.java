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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common functionality for the OS and OSI grids.
 * 
 * @author Tom Gibara
 */

class GridHelper {

	private static final Pattern sNoSquares = Pattern.compile("([EW])\\s*(\\d+)\\s*([NS])\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern sDoubleSquares = Pattern.compile("([A-HJ-Z]{2})(?:\\s*((?:\\d{1}\\s*\\d{1})|(?:\\d{2}\\s*\\d{2})|(?:\\d{3}\\s*\\d{3})|(?:\\d{4}\\s*\\d{4})|(?:\\d{5}\\s*\\d{5})))?", Pattern.CASE_INSENSITIVE);
	private static final Pattern sSingleSquares = Pattern.compile("([A-HJ-Z])(?:\\s*((?:\\d{1}\\s*\\d{1})|(?:\\d{2}\\s*\\d{2})|(?:\\d{3}\\s*\\d{3})|(?:\\d{4}\\s*\\d{4})|(?:\\d{5}\\s*\\d{5})))?", Pattern.CASE_INSENSITIVE);
	private static final int[] sScales = {100000, 10000, 1000, 100, 10, 1};
	private static final int[] sCenters = {50000, 5000, 500, 50, 5, 0};
	private static final String[] sDoubleFormats = {
		"%s%s",
		"%s%s%d%d",
		"%s%s%02d%02d",
		"%s%s %03d %03d",
		"%s%s %04d %04d",
		"%s%s %05d %05d",
		};

	private static final String[] sSingleFormats = {
		"%s",
		"%s%d%d",
		"%s%02d%02d",
		"%s %03d %03d",
		"%s %04d %04d",
		"%s %05d %05d",
		};

	private static int coordinate(String str) {
		return str.isEmpty() ? 0 : Integer.parseInt(str);
	}
	
	private static char indexToChar(int i) {
		if (i < 0 || i > 24) return '?';
		return (char) (i > 7 ? 'B' + i : 'A' + i);
	}
	
	private final boolean singleLetter;
	
	GridHelper(boolean singleLetter) {
		this.singleLetter = singleLetter;
	}
	
	GridRef refFromString(GridRefSystem system, String str) {
		// simple case first
		Matcher matcher = sNoSquares.matcher(str);
		if (matcher.matches()) {
			int easting = Integer.parseInt(matcher.group(2));
			int northing = Integer.parseInt(matcher.group(4));
			if (matcher.group(1).equals("W")) easting = -easting;
			if (matcher.group(3).equals("S")) northing = -northing;
			return system.createGridRef(easting, northing);
		}

		Pattern pattern = singleLetter ? sSingleSquares : sDoubleSquares;
		matcher = pattern.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException("Invalid reference: " + str);
		
		int e, n;
		if (singleLetter) {
			char k = Character.toUpperCase( str.charAt(0) );
			int j = k > 'I' ? k - 'B' : k - 'A';
			e = (j % 5) * 100000;
			n = (4 - (j / 5)) * 100000;
		} else {
			char c = Character.toUpperCase( str.charAt(0) );
			char k = Character.toUpperCase( str.charAt(1) );
			int i = c > 'I' ? c - 'B' : c - 'A';
			int j = k > 'I' ? k - 'B' : k - 'A';
			e = (((i - 2) % 5) * 5 + (j % 5)) * 100000;
			n = (19 - (i / 5) * 5 - (j / 5)) * 100000;
		}

		String coords = matcher.group(2);
		if (coords == null) coords = "";
		int length = coords.indexOf(' ');
		if (length == -1) length = coords.length() / 2;
		int x = coordinate(coords.substring(0, length));
		int y = coordinate(coords.substring(coords.length() - length));
		
		int scale = sScales[length];
		int center = sCenters[length];
		
		return system.createGridRef(
				e + x * scale + center,
				n + y * scale + center
				);
	}

	String refToString(GridRef ref) {
		int easting = ref.getEasting();
		int northing = ref.getNorthing();

		char c, k;
		if (singleLetter) {
			int e = easting / 100000;
			int n = northing / 100000;
			System.out.println(e + " " + n);
			int j = 5 * (4 - n) + e;
			c = indexToChar(-1);
			k = indexToChar(j);
		} else {
			int e1 = easting / 500000;
			int n1 = northing / 500000;
			int e2 = (easting - e1 * 500000) / 100000;
			int n2 = (northing - n1 * 500000) / 100000;
			int i = e1+2 + 5 * (3-n1);
			int j = 5 * (4 - n2) + e2;
			c = indexToChar(i);
			k = indexToChar(j);
		}
		easting = easting % 100000;
		northing = northing % 100000;

		int length;
		int x = easting;
		int y = northing;

		for (length = 0; length < 5; length++) {
			int scale = sScales[length];
			int center = sCenters[length];
			if ((easting - center) % scale == 0 && (northing - center) % scale == 0) {
				x = (easting - center) / scale;
				y = (northing - center) / scale;
				break;
			}
		}

		if (singleLetter) {
			return String.format(sSingleFormats[length], k, x, y);
		} else {
			return String.format(sDoubleFormats[length], c, k, x, y);
		}
	}
	
}
