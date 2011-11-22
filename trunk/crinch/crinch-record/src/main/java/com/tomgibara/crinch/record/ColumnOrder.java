package com.tomgibara.crinch.record;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnOrder {

	static HashSource<ColumnOrder> hashSource = new HashSource<ColumnOrder>() {
		
		@Override
		public void sourceData(ColumnOrder order, WriteStream out) {
			out.writeInt(order.precedence);
			out.writeBoolean(order.ascending);
			out.writeBoolean(order.nullFirst);
		}
	};

	private final int precedence;
	private final boolean ascending;
	private final boolean nullFirst;
	
	public ColumnOrder(int precedence, boolean ascending, boolean nullFirst) {
		if (precedence < 0) throw new IllegalArgumentException("negative precedence");
		this.precedence = precedence;
		this.ascending = ascending;
		this.nullFirst = nullFirst;
	}
	
	public int getPrecedence() {
		return precedence;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public boolean isNullFirst() {
		return nullFirst;
	}
	
}