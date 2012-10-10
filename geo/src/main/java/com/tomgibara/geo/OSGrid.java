package com.tomgibara.geo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSGrid implements Grid {

	public static OSGrid instance = new OSGrid();
	
	private static final Pattern sSquares = Pattern.compile("([A-HJ-Z]{2})(?:\\s*((?:\\d{1}\\s*\\d{1})|(?:\\d{2}\\s*\\d{2})|(?:\\d{3}\\s*\\d{3})|(?:\\d{4}\\s*\\d{4})|(?:\\d{5}\\s*\\d{5})))?", Pattern.CASE_INSENSITIVE);
	private static final int[] sScales = {100000, 10000, 1000, 100, 10, 1};
	private static final int[] sCenters = {50000, 5000, 500, 50, 5, 0};
	private static final String[] sFormats = {
		"%s%s",
		"%s%s%d%d",
		"%s%s%02d%02d",
		"%s%s %03d %03d",
		"%s%s %04d %04d",
		"%s%s %05d %05d",
		};
	
	private static int coordinate(String str) {
		return str.isEmpty() ? 0 : Integer.parseInt(str);
	}
	
	private OSGrid() {
	}
	
	public GridRef refFromString(GridRefSystem system, String str) {
		Matcher matcher = sSquares.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException("Invalid reference: " + str);
		char c = Character.toUpperCase( str.charAt(0) );
		char k = Character.toUpperCase( str.charAt(1) );
		int i = c > 'I' ? c - 'B' : c - 'A';
		int j = k > 'I' ? k - 'B' : k - 'A';
		
		int e = (((i - 2) % 5) * 5 + (j % 5)) * 100000;
		int n = (19 - (i / 5) * 5 - (j / 5)) * 100000;

		String coords = matcher.group(2);
		if (coords == null) coords = "";
		int length = coords.indexOf(' ');
		if (length == -1) length = coords.length() / 2;
		int x = coordinate(coords.substring(0, length));
		int y = coordinate(coords.substring(coords.length() - length));
		
		int scale = sScales[length];
		int center = sCenters[length];
		
		return new GridRef(
				system,
				e + x * scale + center,
				n + y * scale + center
				);
	}

	public String refToString(GridRef ref) {
		int easting = ref.getEasting();
		int northing = ref.getNorthing();
		
		int e1 = easting / 500000;
		int n1 = northing / 500000;
		int e2 = (easting - e1 * 500000) / 100000;
		int n2 = (northing - n1 * 500000) / 100000;
		
		int i = e1+2 + 5 * (3-n1);
		int j = 5 * e2 + n2;
		
		char c = (char) (i > 7 ? 'B' + i : 'A' + i);
		char k = (char) (j > 7 ? 'B' + j : 'A' + j);
		
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
		
		return String.format(sFormats[length], c, k, x, y);
	}

}
