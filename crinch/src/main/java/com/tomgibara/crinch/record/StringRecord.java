package com.tomgibara.crinch.record;

public class StringRecord {

	private final String[] values;
	
	public StringRecord(String[] values) {
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
		return new StringRecord(mapped);
	}
	
	private void checkIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= values.length) throw new IllegalArgumentException("index too large");
	}
	
}
