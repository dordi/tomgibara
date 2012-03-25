package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.record.AbstractConsumer;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.process.ProcessContext;

public class TypeConsumer extends AbstractConsumer<LinearRecord> {

	private ProcessContext context = null;
	private RecordTyper typer;

	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
	}

	@Override
	public int getRequiredPasses() {
		return context.getColumnTypes() == null ? 1 : 0;
	}
	
	@Override
	public void beginPass() {
		context.setPassName("Identifying types");
		typer = new RecordTyper(context);
	}
	
	@Override
	public void consume(LinearRecord record) {
		typer.type(record);
	}
	
	@Override
	public void endPass() {
		context.setRecordCount(typer.getRecordCount());
		context.setColumnTypes(typer.getColumnTypes());
	}

}
