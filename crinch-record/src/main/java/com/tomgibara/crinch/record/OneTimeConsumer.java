package com.tomgibara.crinch.record;

import com.tomgibara.crinch.record.process.ProcessContext;

public class OneTimeConsumer<R extends Record> implements RecordConsumer<R> {

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
	public void consume(R record) {
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
