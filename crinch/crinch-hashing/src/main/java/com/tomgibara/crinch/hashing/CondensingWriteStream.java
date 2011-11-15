package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.AbstractWriteStream;

public class CondensingWriteStream extends AbstractWriteStream {

	private long condensedValue = 0L;
	
	@Override
	public void writeByte(byte v) {
		condensedValue = condensedValue * 31 + v & 0xff;
	}

	@Override
	public void writeChar(char v) {
		condensedValue = condensedValue * 31 + v;
	}

	@Override
	public void writeShort(short v) {
		condensedValue = condensedValue * 31 + v & 0xffff;
	}
	
	@Override
	public void writeInt(int v) {
		condensedValue = condensedValue * 31 + v & 0xffffffffL;
	}

	@Override
	public void writeLong(long v) {
		condensedValue = condensedValue * 31 + v;
	}

	public long getCondensedValue() {
		return condensedValue;
	}
	
}
