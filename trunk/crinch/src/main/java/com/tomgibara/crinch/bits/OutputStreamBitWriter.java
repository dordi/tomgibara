package com.tomgibara.crinch.bits;

import java.io.IOException;
import java.io.OutputStream;

//TODO not yet optimized
public class OutputStreamBitWriter extends AbstractBitWriter {

	private final OutputStream out;
	
	private int buffer = 0;
	private int count = 0;
	private long position = 0;
	
	public OutputStreamBitWriter(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public int writeBit(int bit) {
		buffer = (buffer << 1) | (bit & 1);
		count++;
		flush();
		position++;
		return 1;
	}
	
	@Override
	public void flush() {
		if (count == 8) {
			try {
				out.write(buffer);
			} catch (IOException e) {
				throw new BitStreamException(e);
			}
			count = 0;
		}
	}

	@Override
	public long getPosition() {
		return position;
	}
	
}
