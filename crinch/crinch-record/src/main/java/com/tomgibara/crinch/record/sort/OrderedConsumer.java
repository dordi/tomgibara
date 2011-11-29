package com.tomgibara.crinch.record.sort;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDefinition;
import com.tomgibara.crinch.record.def.SubRecordDefinition;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;

public abstract class OrderedConsumer implements RecordConsumer<LinearRecord> {

	private final SubRecordDefinition subRecDef;
	
	ProcessContext context;
	RecordDefinition definition;
	DynamicRecordFactory factory;

	public OrderedConsumer(SubRecordDefinition subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		RecordDefinition def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		definition = def.asBasis();
		if (subRecDef != null) definition = definition.asSubRecord(subRecDef);
	}

	@Override
	public void beginPass() {
		factory = DynamicRecordFactory.getInstance(definition);
	}
	
	File sortedFile(boolean input) {
		return context.file("compact", false, input ? definition.getBasis() : definition);
	}
	

}
