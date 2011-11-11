package com.tomgibara.crinch.bits;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class OutputStreamBitWriter extends ByteBasedBitWriter {

	private static byte[] sZerosBuffer = null;
	private static byte[] sOnesBuffer = null;
	
	private static final int PAD_BUFFER = 100;
	private static final int PAD_LIMIT = 3;
	private final OutputStream out;
	
	public OutputStreamBitWriter(OutputStream out) {
		this.out = out;
	}

	@Override
	protected void writeByte(int value) throws BitStreamException {
		try {
			out.write(value);
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}
	
	@Override
	protected long padBytes(boolean padWithOnes, long count) throws BitStreamException {
		try {
			if (count < PAD_LIMIT) {
				final int value = padWithOnes ? 0xff : 0x00;
				for (int i = 0; i < count; i++) out.write(value);
			} else if (count < PAD_BUFFER) {
				out.write(getBuffer(padWithOnes), 0, (int) count);
			} else {
				byte[] buffer = getBuffer(padWithOnes);
				long i;
				for (i = 0; i < count; i += PAD_BUFFER) {
					out.write(buffer);
				}
				int r = (int) (count - i);
				if (r != 0) out.write(buffer, 0, r);
			}
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
		return count;
	}
	
	@Override
	protected long seekByte(long index) throws BitStreamException {
		return -1L;
	}

	private byte[] getBuffer(boolean withOnes) {
		byte[] buffer = withOnes ? sOnesBuffer : sZerosBuffer;
		if (buffer == null) {
			buffer = new byte[PAD_BUFFER];
			if (withOnes) {
				Arrays.fill(buffer, (byte) 255);
				sOnesBuffer = buffer;
			} else {
				sZerosBuffer = buffer;
			}
		}
		return buffer;
	}
	
}
