package com.tomgibara.geo;

public interface Grid {

	int[] refFromString(String str);
	
	String refToString(int easting, int northing);

}
