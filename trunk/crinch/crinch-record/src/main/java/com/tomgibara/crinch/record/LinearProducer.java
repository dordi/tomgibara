package com.tomgibara.crinch.record;

public class LinearProducer extends AdaptedProducer<StringRecord, LinearRecord> {

	private ColumnParser parser;
	
	public LinearProducer(RecordProducer<StringRecord> producer) {
		super(producer);
	}

	@Override
	public void prepare(ProcessContext context) {
		super.prepare(context);
		parser = context.getColumnParser();
	}
	
	@Override
	protected LinearRecord adapt(StringRecord record) {
		return new ParsedRecord(parser, record);
	}
	
}
