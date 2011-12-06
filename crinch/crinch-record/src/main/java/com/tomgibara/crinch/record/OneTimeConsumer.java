package com.tomgibara.crinch.record;

public abstract class OneTimeConsumer<R extends Record> implements RecordConsumer<R> {

	private boolean done = false;
	
	@Override
	public void prepare(ProcessContext context) {
	}

	@Override
	public int getRequiredPasses() {
		return done ? 0 : 1;
	}

	@Override
	public void beginPass() {
	}

	@Override
	public void endPass() {
		done = true;
	}

	@Override
	public void complete() {
	}

	@Override
	public void quit() {
	}

}
