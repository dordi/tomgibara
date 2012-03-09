package com.tomgibara.crinch.bits;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelBitReader extends ByteBasedBitReader {

	private final FileChannel channel;
	private final ByteBuffer buffer;
	private long bufferPosition;
	
	public FileChannelBitReader(FileChannel channel, int bufferSize, boolean direct) {
		if (channel == null) throw new IllegalArgumentException("null channel");
		if (bufferSize < 1) throw new IllegalArgumentException("non-positive buffer size");
		this.channel = channel;
		buffer = direct ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
		// force buffer to be populated
		buffer.position(buffer.limit());
		bufferPosition = -1L;
	}
	
	@Override
	protected int readByte() throws BitStreamException {
		if (buffer.hasRemaining()) return buffer.get() & 0xff;
		buffer.limit(buffer.capacity()).position(0);
		try {
			bufferPosition = channel.position();
			channel.read(buffer);
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
		buffer.flip();
		return buffer.hasRemaining() ? buffer.get() & 0xff : -1;
	}
	
	@Override
	protected long seekByte(long index) throws BitStreamException {
		// first see if index is inside buffer
		if (bufferPosition >= 0) {
			long offset = index - bufferPosition;
			if (offset >= 0 && offset <= buffer.limit()) {
				buffer.position((int) offset);
				return index;
			}
		}
		return seekSlow(index);
	}
	
	@Override
	protected long skipBytes(long count) throws BitStreamException {
		// optimized code path, where skip fits inside buffer
		if (count <= buffer.remaining()) {
			buffer.position(buffer.position() + (int) count);
			return count;
		}
		
		// otherwise delegate to seek
		long position;
		if (bufferPosition >= 0) {
			// if we have a buffer, skip relative to it's resolved position
			position = bufferPosition + buffer.position();
		} else {
			try {
				position = channel.position();
			} catch (IOException e) {
				throw new BitStreamException(e);
			}
		}
		return seekSlow(position + count) - position;
	}

	FileChannel getChannel() {
		return channel;
	}
	
	private long seekSlow(long index) throws BitStreamException {
		try {
			long length = channel.size();
			if (index >= length) index = length;
			channel.position(index);
			buffer.position(buffer.limit());
			bufferPosition = -1L;
			return index;
		} catch (IOException e) {
			throw new BitStreamException(e);
		}
	}
	
}
