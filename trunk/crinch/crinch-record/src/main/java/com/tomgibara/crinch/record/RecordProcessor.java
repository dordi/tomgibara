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
			producer.prepare(context);
			state = 1;
			consumer.prepare(context);
			state = 2;
			new RecordTransfer<R>(producer, consumer).transfer(context);
			state = 3;
			consumer.complete();
			state = 4;
			producer.complete();
			state = 5;
		} catch (RuntimeException e) {
			context.log("error processing records", e);
		} finally {
			if (state == 4) {
				state = 0;
			}
			if (state == 3) {
				state = 1;
			}
			if (state == 2) {
				try {
					consumer.quit();
				} catch (RuntimeException ex) {
					context.log("error terminating consumption", ex);
				} finally {
					state = 1;
				}
			}
			if (state == 1) {
				try {
					producer.complete();
				} catch (RuntimeException ex) {
					context.log("error terminating production", ex);
				} finally {
					state = 0;
				}
			}
		}
		return state == 5;
	}
	
}
