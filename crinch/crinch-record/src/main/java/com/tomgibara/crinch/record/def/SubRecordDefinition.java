package com.tomgibara.crinch.record.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubRecordDefinition {

	private static final int[] NO_INDICES = new int[0];
	private static final List<ColumnOrder.Indexed> NO_ORDERS = Collections.emptyList();
	
	final boolean ordinalEliminated;
	final boolean positionEliminated;
	final int[] indices;
	final List<ColumnOrder.Indexed> orders;
	
	public SubRecordDefinition(boolean ordinalEliminated, boolean positionEliminated, int[] indices, List<ColumnOrder.Indexed> orders) {
		this.ordinalEliminated = ordinalEliminated;
		this.positionEliminated = positionEliminated;
		//TODO should check arguments as far as possible
		this.indices = indices == null ? NO_INDICES : indices.clone();
		this.orders = orders == null ? NO_ORDERS : Collections.unmodifiableList(new ArrayList<ColumnOrder.Indexed>(orders));
	}
	
	public boolean isOrdinalEliminated() {
		return ordinalEliminated;
	}
	
	public boolean isPositionEliminated() {
		return positionEliminated;
	}
	
	public int[] getIndices() {
		return indices.clone();
	}
	
	public int getColumnCount() {
		return indices.length;
	}
	
	public List<ColumnOrder.Indexed> getOrders() {
		return orders;
	}
}
