package com.tomgibara.crinch.perm.permutable;

import java.util.Arrays;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableBooleans implements Permutable {

	private final boolean[] values;
	
	public PermutableBooleans(boolean[] values) {
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}
	
	public boolean[] getValues() {
		return values;
	}
	
	@Override
	public int getPermutableSize() {
		return values.length;
	}
	
	@Override
	public PermutableBooleans transpose(int i, int j) {
		boolean v = values[i];
		values[i] = values[j];
		values[j] = v;
		return this;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableBooleans)) return false;
		PermutableBooleans that = (PermutableBooleans) obj;
		return Arrays.equals(this.values, that.values);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}
