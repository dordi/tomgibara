package com.tomgibara.crinch.record;

public abstract class AbstractRecord implements Record {

	final long recordOrdinal;
	final long recordPosition;
	
	public AbstractRecord() {
		recordOrdinal = -1L;
		recordPosition = -1L;
	}

	public AbstractRecord(long recordOrdinal, long recordPosition) {
		this.recordOrdinal = recordOrdinal;
		this.recordPosition = recordPosition;
	}
	
	public AbstractRecord(AbstractRecord that) {
		this.recordOrdinal = that.recordOrdinal;
		this.recordPosition = that.recordPosition;
	}
	
	public AbstractRecord(Record that) {
		this.recordOrdinal = that.getRecordOrdinal();
		this.recordPosition = that.getRecordPosition();
	}

	@Override
	public long getRecordOrdinal() {
		return recordOrdinal;
	}
	
	@Override
	public long getRecordPosition() {
		return recordPosition;
	}
	
}
