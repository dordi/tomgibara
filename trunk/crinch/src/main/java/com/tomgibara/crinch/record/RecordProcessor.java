package com.tomgibara.crinch.record;

public class RecordProcessor {

	private ProcessContext context = null;
	
	public RecordProcessor() {
	}

	public RecordProcessor(ProcessContext context) {
		this.context = context;
	}
	
	public void setContext(ProcessContext context) {
		this.context = context;
	}
	
	public ProcessContext getContext() {
		return context;
	}
	
	public <R extends Record> boolean process(RecordProducer<R> producer, RecordConsumer<R> consumer) {
		if (context == null) throw new IllegalStateException("null context");
		int state = 0;
		try {
			consumer.prepare(context);
			state = 1;
			new RecordTransfer<R>(producer, consumer).transfer(context);
			state = 2;
		} catch (RuntimeException e) {
			context.log("error processing records", e);
		} finally {
			try {
				switch (state) {
				case 1: consumer.quit(); break;
				case 2: consumer.complete(); state = 3; break;
				}
			} catch (RuntimeException ex) {
				context.log("error terminating consumer", ex);
			}
		}
		return state == 3;
	}
	
}
