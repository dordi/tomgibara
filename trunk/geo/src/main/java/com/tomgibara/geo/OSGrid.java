package com.tomgibara.geo;


public final class OSGrid implements Grid {

	public static OSGrid instance = new OSGrid();
	
	private final GridHelper helper = new GridHelper(false);
	
	private OSGrid() { }
	
	@Override
	public GridRef refFromString(GridRefSystem system, String str) {
		return helper.refFromString(system, str);
	}
	
	@Override
	public String refToString(GridRef ref) {
		return helper.refToString(ref);
	}

}