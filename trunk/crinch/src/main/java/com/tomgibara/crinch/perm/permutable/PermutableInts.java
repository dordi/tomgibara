package com.tomgibara.crinch.perm.permutable;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableInts implements Permutable {

	private final int[] values;
	
	public PermutableInts(int[] values) {
		if (values == null) throw new IllegalArgumentException("null values");
		this.values = values;
	}
	
	@Override
	public int getPermutableSize() {
		return values.length;
	}
	
	@Override
	public PermutableInts swap(int i, int j) {
		int v = values[i];
		values[i] = values[j];
		values[j] = v;
		return this;
	}
	
}
