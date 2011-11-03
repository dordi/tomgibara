package com.tomgibara.crinch.record;

public class LinearProducer extends AdaptedProducer<StringRecord, LinearRecord> {

	private final ColumnParser parser;
	
	public LinearProducer(RecordProducer<StringRecord> producer, ColumnParser parser) {
		super(producer);
		if (parser == null) throw new IllegalArgumentException("null parser");
		this.parser = parser;
	}

	@Override
	protected LinearRecord adapt(StringRecord record) {
		return new ParsedRecord(parser, record);
	}
	
}
