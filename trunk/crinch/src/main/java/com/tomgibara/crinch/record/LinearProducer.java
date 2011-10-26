package com.tomgibara.crinch.record;

public class LinearProducer extends AdaptedProducer<StringRecord, LinearRecord> {

	private final ValueParser parser;
	
	public LinearProducer(RecordProducer<StringRecord> producer, ValueParser parser) {
		super(producer);
		if (parser == null) throw new IllegalArgumentException("null parser");
		this.parser = parser;
	}

	@Override
	protected LinearRecord adapt(StringRecord record) {
		return new ParsedRecord(parser, record);
	}
	
}
