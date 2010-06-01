package com.tomgibara.crinch.util;

public interface WriteStream {

	void writeByte(byte v);

	void writeBytes(byte bs[]);
	
	void writeBytes(byte bs[], int off, int len);

	void writeInt(int v);
	
	void writeBoolean(boolean v);

	void writeShort(short v);
	
	void writeLong(long v);
	
	void writeFloat(float v);
	
	void writeDouble(float v);
	
	void writeChar(char v);
	
	void writeChars(char[] cs);

	void writeChars(char[] cs, int off, int len);
	
	void writeString(String v);

}
