package com.tomgibara.crinch.perm.permutable;

import java.util.Arrays;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableBytes implements Permutable {

	private final byte[] values;
	
	public PermutableBytes(byte[] values) {
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}
	
	public byte[] getValues() {
		return values;
	}
	
	@Override
	public int getPermutableSize() {
		return values.length;
	}
	
	@Override
	public PermutableBytes transpose(int i, int j) {
		byte v = values[i];
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
		if (!(obj instanceof PermutableBytes)) return false;
		PermutableBytes that = (PermutableBytes) obj;
		return Arrays.equals(this.values, that.values);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values);
	}
	
}
