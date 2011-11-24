package com.tomgibara.crinch.record.def;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnOrder {

	// statics
	
	static HashSource<ColumnOrder> hashSource = new HashSource<ColumnOrder>() {
		
		@Override
		public void sourceData(ColumnOrder order, WriteStream out) {
			out.writeInt(order.precedence);
			out.writeBoolean(order.ascending);
			out.writeBoolean(order.nullFirst);
		}
	};

	// fields
	
	private final int precedence;
	private final boolean ascending;
	private final boolean nullFirst;
	
	// constructors
	
	public ColumnOrder(int precedence, boolean ascending, boolean nullFirst) {
		if (precedence < 0) throw new IllegalArgumentException("negative precedence");
		this.precedence = precedence;
		this.ascending = ascending;
		this.nullFirst = nullFirst;
	}
	
	// accessors
	
	public int getPrecedence() {
		return precedence;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public boolean isNullFirst() {
		return nullFirst;
	}
	
	//TODO object methods
	
	// inner classes
	
	public static class Indexed {
		
		private final int index;
		private final ColumnOrder order;
		
		public Indexed(int index, ColumnOrder order) {
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (order == null) throw new IllegalArgumentException("null order");
			
			this.index = index;
			this.order = order;
		}
		
		public int getIndex() {
			return index;
		}
		
		public ColumnOrder getOrder() {
			return order;
		}
		
	}
	
}