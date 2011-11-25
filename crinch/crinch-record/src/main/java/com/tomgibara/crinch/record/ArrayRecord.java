package com.tomgibara.crinch.record;

import java.util.Arrays;

public class ArrayRecord extends AbstractLinearRecord {

	private final Object[] values;
	
	public ArrayRecord(long recordOrdinal, long recordPosition, Object[] values) {
		super(recordOrdinal, recordPosition);
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}

	@Override
	protected Object getValue(int index, boolean free) {
		if (free) {
			Object tmp = values[index];
			values[index] = null;
			return tmp;
		} else {
			return values[index];
		}
	}

	@Override
	protected int getLength() {
		return values.length;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}
