package com.tomgibara.crinch.record;

public interface LinearRecord {

	boolean hasNext();
	
	String nextString();

	char nextChar();

	boolean nextBoolean();
	
	byte nextByte();

	short nextShort();
	
	int nextInt();

	long nextLong();

	float nextFloat();
	
	double nextDouble();
	
	void skipNext();
	
	boolean wasNull();
	
	boolean wasInvalid();
	
	void exhaust();
}
