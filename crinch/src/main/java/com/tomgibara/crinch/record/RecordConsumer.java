package com.tomgibara.crinch.record;

public interface RecordConsumer<R extends Record> {

	void prepare(ProcessContext context);

	int getRequiredPasses();
	
	void beginPass();
	
	void consume(R record);
	
	void endPass();
	
	void complete();

	// stop immediately without further processing
	void quit();
	
}
