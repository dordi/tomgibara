package com.tomgibara.crinch.record;


public class RecordTransfer<R extends Record> {

	private final RecordProducer<R> producer;
	private final RecordConsumer<R> consumer;
	
	private long recordNumber = 0L;
	
	public RecordTransfer(RecordProducer<R> producer, RecordConsumer<R> consumer) {
		if (producer == null) throw new IllegalArgumentException("null producer");
		if (consumer == null) throw new IllegalArgumentException("null consumer");
		this.producer = producer;
		this.consumer = consumer;
	}
	
	public void transfer(ProcessContext context) {
		while (consumer.getRequiredPasses() != 0) {
			RecordSequence<R> sequence = producer.open();
			if (sequence == null) throw new RuntimeException("null record sequence from producer");
			try {
				consumer.beginPass();
				context.setRecordsTransferred(recordNumber = 0);
				while (sequence.hasNext()) {
					consumer.consume(sequence.next());
					context.setRecordsTransferred(++recordNumber);
				}
				consumer.endPass();
			} finally {
				try {
					sequence.close();
				} catch (RuntimeException e) {
					context.log("error closing sequence", e);
				}
			}
		}
	}
}
