package com.tomgibara.crinch.record;

public class LinearProducer extends AdaptedProducer<StringRecord, LinearRecord> {

	private ColumnParser parser;
	
	public LinearProducer(RecordProducer<StringRecord> producer) {
		super(producer);
	}

	@Override
	public RecordSequence<LinearRecord> open(ProcessContext context) {
		parser = context.getColumnParser();
		return super.open(context);
	}
	
	@Override
	protected LinearRecord adapt(StringRecord record) {
		return new ParsedRecord(parser, record);
	}
	
}
