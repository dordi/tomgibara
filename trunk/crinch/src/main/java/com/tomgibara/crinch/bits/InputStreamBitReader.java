package com.tomgibara.crinch.bits;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamBitReader extends ByteBasedBitReader {

	private final InputStream in;
	
	public InputStreamBitReader(InputStream in) {
		this.in = in;
	}

	@Override
	protected int readByte() throws BitStreamException {
		try {
			return in.read();
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}

	@Override
	protected long skipBytes(long count) throws BitStreamException {
		try {
			return in.skip(count);
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}

	@Override
	protected long seekByte(long index) throws BitStreamException {
		return -1L;
	}
	
}
