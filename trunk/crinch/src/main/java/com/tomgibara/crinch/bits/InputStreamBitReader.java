package com.tomgibara.crinch.bits;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

//TODO not optimized
public class InputStreamBitReader extends AbstractBitReader {

	private final InputStream in;
	
	private int buffer = 0;
	private int count = 0;
	private int bitsRead = 0;
	
	public InputStreamBitReader(InputStream in) {
		this.in = in;
	}
	
	@Override
	public int readBit() {
		if (count == 0) {
			try {
				buffer = in.read();
				if (buffer == -1) throw new EOFException();
				count = 8;
			} catch (IOException e) {
				throw new BitStreamException(e);
			}
		}
		bitsRead++;
		return (buffer >> --count) & 1;
	}
	
	public int skipToByteBoundary() {
		if (count == 0) return 0;
		int r = 8 - count;
		bitsRead += r;
		count = 0;
		return r;
	}

	public int skipToShortBoundary() {
		return skip(16); 
	}
	
	public int skipToIntBoundary() {
		return skip(32); 
	}
	
	public int skipToLongBoundary() {
		return skip(64); 
	}
	
	// TODO test
	private int skip(int size) {
		int r = skipToByteBoundary();
		int a = bitsRead >> 3;
		int b = size >> 3;
		for (int c = a & (b - 1); c > 0; c--) {
			try {
				in.read();
			} catch (IOException e) {
				throw new BitStreamException(e);
			}
			r += 8;
		}
		return r;
	}

}
