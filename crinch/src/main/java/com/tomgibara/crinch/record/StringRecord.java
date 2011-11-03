package com.tomgibara.crinch.record;

public class StringRecord extends AbstractRecord {

	private final String[] values;
	
	public StringRecord(long recordOrdinal, long recordPosition, String[] values) {
		super(recordOrdinal, recordPosition);
		if (values == null) throw new IllegalArgumentException("null value");
		this.values = values;
	}
	
	public String get(int index) {
		checkIndex(index);
		String value = values[index];
		return value == null || value.isEmpty() ? null : value;
	}
	
	public int length() {
		return values.length;
	}

	public StringRecord mappedRecord(int... mapping) {
		String[] mapped = new String[mapping.length];
		for (int i = 0; i < mapped.length; i++) {
			int index = mapping[i];
			checkIndex(index);
			mapped[i] = values[index];
		}
		return new StringRecord(recordOrdinal, -1L, mapped);
	}
	
	private void checkIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= values.length) throw new IllegalArgumentException("index too large");
	}
	
}
