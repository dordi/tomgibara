/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


public class MemoryWriterTest extends AbstractByteBasedBitWriterTest {

	@Override
	BitWriter newBitWriter(long size) {
		return new MemoryBitWriter(new int[(int) ((size + 31) / 32)], size);
	}

	@Override
	BitReader bitReaderFor(BitWriter writer) {
		MemoryBitWriter mw = (MemoryBitWriter) writer;
		return new MemoryBitReader(mw.getMemory(), mw.getSize());
	}
	
}
