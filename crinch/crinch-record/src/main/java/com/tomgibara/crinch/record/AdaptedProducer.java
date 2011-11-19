package com.tomgibara.crinch.record;

public abstract class AdaptedProducer<R extends Record, S extends Record> implements RecordProducer<S> {

	protected final RecordProducer<R> producer;
	
	public AdaptedProducer(RecordProducer<R> producer) {
		if (producer == null) throw new IllegalArgumentException("null producer");
		this.producer = producer;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		producer.prepare(context);
	}
	
	@Override
	public RecordSequence<S> open() {
		return new AdaptedSequence<R, S>(producer.open()) {
			@Override
			public S next() {
				return adapt(sequence.next());
			}
		};
	}
	
	@Override
	public void complete() {
		producer.complete();
	}
	
	protected abstract S adapt(R record);
	
}
