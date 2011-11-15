package com.tomgibara.crinch.record;

//TODO should probably move into the record package as ColumnOrder
public class ColumnOrder {
	
	private final int index;
	private final boolean ascending;
	private final boolean nullFirst;
	
	public ColumnOrder(int index, boolean ascending, boolean nullFirst) {
		this.index = index;
		this.ascending = ascending;
		this.nullFirst = nullFirst;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public boolean isNullFirst() {
		return nullFirst;
	}
	
}