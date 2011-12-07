package com.tomgibara.crinch.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.record.process.ProcessContext;

public class SequentialConsumer<R extends Record> implements RecordConsumer<R> {

	private final List<RecordConsumer<R>> consumers;
	private final int count;
	private ProcessContext context;
	private int next = 0;
	private int state = 0;
	// store reference for efficiency - set only for duration of pass
	private RecordConsumer<R> consumer;

	public SequentialConsumer(RecordConsumer<R>... consumers) {
		this(consumers == null ? null : Arrays.asList(consumers));
	}

	public SequentialConsumer(List<RecordConsumer<R>> consumers) {
		if (consumers == null) throw new IllegalArgumentException("null consumers");
		for (RecordConsumer<R> consumer : consumers) {
			if (consumer == null) throw new IllegalArgumentException("consumers contains null");
		}
		this.consumers = new ArrayList<RecordConsumer<R>>(consumers);
		count = consumers.size();
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
	}

	private void advance() {
		while (next < count) {
			consumer = consumers.get(next);
			switch (state) {
			case 0 :
				consumer.prepare(context);
				state = 1;
				break;
			case 1 :
				if (consumer.getRequiredPasses() > 0) return;
				state = 2;
				break;
			case 2 :
				consumer.complete();
				next++;
				state = 0;
				break;
			}
		}
		consumer = null;
	}
	
	@Override
	public int getRequiredPasses() {
		advance();
		return next < count ? 1 : 0;
	}

	@Override
	public void beginPass() {
		advance();
		if (consumer == null) throw new IllegalStateException("no next consumer: possible inconsistent return from getRequiredPasses()");
		consumer.beginPass();
	}

	@Override
	public void consume(R record) {
		consumer.consume(record);
	}

	@Override
	public void endPass() {
		consumer.endPass();
		advance();
	}

	@Override
	public void complete() {
		advance();
	}

	@Override
	public void quit() {
		if (next < count && state == 1) {
			consumers.get(next).quit();
		}
	}

}
