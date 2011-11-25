package com.tomgibara.crinch.record;

public class SingletonRecord extends AbstractLinearRecord {

	private Object value;
	
	public SingletonRecord(long recordOrdinal, long recordPosition, Object value) {
		super(recordOrdinal, recordPosition);
		this.value = value;
	}

	@Override
	protected Object getValue(int index, boolean free) {
		if (free) {
			Object tmp = value;
			value = null;
			return tmp;
		} else {
			return value;
		}
	}

	@Override
	protected int getLength() {
		return 1;
	}

	@Override
	public String toString() {
		return "ordinal: " + recordOrdinal + ",  position: " + recordPosition + ", value: " + value;
	}
	
}
