package com.tomgibara.crinch.util;

public abstract class AbstractWriteStream implements WriteStream {

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}
	
	@Override
	public void writeDouble(float v) {
		writeLong(Double.doubleToLongBits(v));
	}
	
	@Override
	public void writeBytes(byte[] bs) {
		writeBytes(bs, 0, bs.length);
	}

	@Override
	public void writeChars(char[] cs) {
		writeChars(cs, 0, cs.length);
	}

	@Override
	public void writeString(String v) {
		final int length = v.length();
		writeInt(length);
		writeChars(v.toCharArray());
	}

}
