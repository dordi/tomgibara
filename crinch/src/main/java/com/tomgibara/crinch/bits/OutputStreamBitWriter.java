package com.tomgibara.crinch.bits;

import java.io.IOException;
import java.io.OutputStream;

//TODO not yet optimized
public class OutputStreamBitWriter extends AbstractBitWriter {

	private final OutputStream out;
	
	private int buffer = 0;
	private int count = 0;
	private int bitsWritten = 0;
	
	public OutputStreamBitWriter(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public int writeBit(int bit) {
		buffer = (buffer << 1) | (bit & 1);
		count++;
		if (count == 8) {
			try {
				out.write(buffer);
			} catch (IOException e) {
				//TODO need a mechanism for this
				throw new BitStreamException(e);
			}
			count = 0;
		}
		bitsWritten++;
		return 1;
	}
	
	@Override
	public void flush() {
		try {
			out.flush();
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}
	
	public int getBitsWritten() {
		return bitsWritten;
	}

	public int padToByteBoundary() {
		if (count == 0) return 0;
		return writeZeros(8 - count);
	}
	
	public int padToShortBoundary() {
		return pad(16); 
	}
	
	public int padToIntBoundary() {
		return pad(32); 
	}
	
	public int padToLongBoundary() {
		return pad(64); 
	}
	
	private int pad(int size) {
		int r = bitsWritten & (size - 1);
		if (r == 0) return 0;
		return writeZeros(size - r);
	}
}
