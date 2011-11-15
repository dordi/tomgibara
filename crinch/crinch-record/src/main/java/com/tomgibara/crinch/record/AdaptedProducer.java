package com.tomgibara.crinch.record;

public abstract class AdaptedProducer<R extends Record, S extends Record> implements RecordProducer<S> {

	protected final RecordProducer<R> producer;
	
	public AdaptedProducer(RecordProducer<R> producer) {
		if (producer == null) throw new IllegalArgumentException("null producer");
		this.producer = producer;
	}
	
	@Override
	public RecordSequence<S> open(final ProcessContext context) {
		return new AdaptedSequence<R, S>(producer.open(context)) {
			@Override
			public S next() {
				return adapt(sequence.next());
			}
		};
	}
	
	protected abstract S adapt(R record);
	
}
