package com.tomgibara.crinch.record;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnDefinition {

	// statics
	
	static HashSource<ColumnDefinition> hashSource = new HashSource<ColumnDefinition>() {
		
		@Override
		public void sourceData(ColumnDefinition definition, WriteStream out) {
			out.writeInt(definition.type.ordinal());
			if (definition.order != null) {
				ColumnOrder.hashSource.sourceData(definition.order, out);
			}
		}
	};

	
	// fields
	
	private final int index;
	private final ColumnType type;
	private final ColumnOrder order;
	private final int basis;

	// constructors
	
	ColumnDefinition(int index, ColumnType type, ColumnOrder order, int basis) {
		this.index = index;
		this.type = type;
		this.order = order;
		this.basis = basis;
	}
	
	// accessors
	
	public int getIndex() {
		return index;
	}
	
	public ColumnType getType() {
		return type;
	}
	
	public ColumnOrder getOrder() {
		return order;
	}
	
	int getBasis() {
		return basis;
	}
	
	//TODO implement object methods
	
}
