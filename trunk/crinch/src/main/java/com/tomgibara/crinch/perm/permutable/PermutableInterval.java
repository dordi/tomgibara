package com.tomgibara.crinch.perm.permutable;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableInterval<P extends Permutable> implements Permutable {

	private final P permutable;
	private final int offset;
	private final int length;
	
	public PermutableInterval(P permutable, int offset, int length) {
		if (permutable == null) throw new IllegalArgumentException("null permutable");
		if (length < 0) throw new IllegalArgumentException("negative length");
		if (offset < 0) throw new IllegalArgumentException("negative offset");
		if (offset + length > permutable.getPermutableSize()) throw new IllegalArgumentException("size exceeded");
		
		this.permutable = permutable;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public int getPermutableSize() {
		return length;
	}
	
	@Override
	public Permutable transpose(int i, int j) {
		permutable.transpose(i + offset, j + offset);
		return this;
	}
	
}
