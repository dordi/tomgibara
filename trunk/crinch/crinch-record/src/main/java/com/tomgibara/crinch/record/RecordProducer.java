package com.tomgibara.crinch.record;

public interface RecordProducer<R extends Record> {

	void prepare(ProcessContext context);
	
	RecordSequence<R> open();
	
	void complete();
	
}
