package com.tomgibara.geo;

public interface Grid {

	GridRef refFromString(GridRefSystem system, String str);
	
	String refToString(GridRef ref);

}
