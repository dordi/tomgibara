package com.tomgibara.crinch.record;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnDefinition {

	// statics
	
	static HashSource<ColumnDefinition> hashSource = new HashSource<ColumnDefinition>() {
		
		@Override
		public void sourceData(ColumnDefinition definition, WriteStream out) {
			out.writeInt(definition.index);
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

	// constructors
	
	public ColumnDefinition(int index, ColumnType type, ColumnOrder order) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (type == null) throw new IllegalArgumentException("null type");
		this.index = index;
		this.type = type;
		this.order = order;
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
	
	//TODO implement object methods
	
}
