package com.tomgibara.crinch.record.sort;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.record.ColumnOrder;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordDefinition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;

public abstract class OrderedConsumer implements RecordConsumer<LinearRecord> {

	private final boolean ordinal;
	private final boolean positional;
	private final ColumnOrder[] orders;
	
	ProcessContext context;
	RecordDefinition definition;
	DynamicRecordFactory factory;

	public OrderedConsumer(boolean ordinal, boolean positional, ColumnOrder... orders) {
		this.ordinal = ordinal;
		this.positional = positional;
		this.orders = orders;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		definition = new RecordDefinition(ordinal, positional, types, orders);
	}

	File sortedFile() {
		return new File(context.getOutputDir(), context.getDataName() + ".compact." + definition.getId());
	}
	

}
