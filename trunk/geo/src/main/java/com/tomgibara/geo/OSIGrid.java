package com.tomgibara.geo;


public final class OSIGrid implements Grid {

	public static OSIGrid instance = new OSIGrid();
	
	private final GridHelper helper = new GridHelper(true);
	
	private OSIGrid() { }
	
	@Override
	public GridRef refFromString(GridRefSystem system, String str) {
		return helper.refFromString(system, str);
	}
	
	@Override
	public String refToString(GridRef ref) {
		return helper.refToString(ref);
	}

}
