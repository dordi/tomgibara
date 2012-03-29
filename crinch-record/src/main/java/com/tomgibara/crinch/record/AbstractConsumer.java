package com.tomgibara.crinch.record;

import com.tomgibara.crinch.record.process.ProcessContext;

public abstract class AbstractConsumer<R extends Record> implements RecordConsumer<R> {

	@Override
	public void prepare(ProcessContext context) {
	}

	@Override
	public void beginPass() {
	}

	@Override
	public void consume(R record) {
	}

	@Override
	public void endPass() {
	}

	@Override
	public void complete() {
	}

	@Override
	public void quit() {
	}

}
