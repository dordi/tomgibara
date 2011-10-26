package com.tomgibara.crinch.record;

public interface RecordProducer<R> {

	public RecordSequence<R> open(ProcessContext context);
	
}
