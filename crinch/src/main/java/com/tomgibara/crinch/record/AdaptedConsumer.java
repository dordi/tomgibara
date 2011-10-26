package com.tomgibara.crinch.record;

public abstract class AdaptedConsumer<R,S> implements RecordConsumer<S> {

	protected final RecordConsumer<R> consumer;

	public AdaptedConsumer(RecordConsumer<R> consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public void complete() {
		consumer.complete();
	}

	@Override
	public int getRequiredPasses() {
		return consumer.getRequiredPasses();
	}

	@Override
	public void prepare(ProcessContext context) {
		consumer.prepare(context);
	}

	@Override
	public void quit() {
		consumer.quit();
	}

	@Override
	public void beginPass() {
		consumer.beginPass();
	}

	@Override
	public void endPass() {
		consumer.endPass();
	}
	
}
