package com.tomgibara.crinch.record;

public class MappedProducer<R extends StringRecord> extends AdaptedProducer<R,StringRecord> {

	private final int[] mapping;

	public MappedProducer(RecordProducer<R> producer, int... mapping) {
		super(producer);
		if (mapping == null) throw new IllegalArgumentException();
		this.mapping = mapping;
	}
	
	@Override
	protected StringRecord adapt(R record) {
		return record.mappedRecord(mapping);
	}
	
	
}
