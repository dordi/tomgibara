package com.tomgibara.crinch.record;

public interface RecordProducer<R extends Record> {

	public RecordSequence<R> open(ProcessContext context);
	
}
