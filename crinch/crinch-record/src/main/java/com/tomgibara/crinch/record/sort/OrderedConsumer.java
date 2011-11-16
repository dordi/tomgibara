package com.tomgibara.crinch.record.sort;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordDefinition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;

public abstract class OrderedConsumer implements RecordConsumer<LinearRecord> {

	private final boolean ordinal;
	private final boolean positional;
	
	ProcessContext context;
	RecordDefinition definition;
	DynamicRecordFactory factory;

	public OrderedConsumer(boolean ordinal, boolean positional) {
		this.ordinal = ordinal;
		this.positional = positional;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		definition = new RecordDefinition(ordinal, positional, types, context.getColumnOrders());
	}

	File sortedFile() {
		return new File(context.getOutputDir(), context.getDataName() + ".compact." + definition.getId());
	}
	

}
