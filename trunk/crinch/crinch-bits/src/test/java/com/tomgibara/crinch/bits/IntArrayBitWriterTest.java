/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;


public class IntArrayBitWriterTest extends AbstractBitWriterTest {

	@Override
	BitWriter newBitWriter(long size) {
		return new IntArrayBitWriter(new int[(int) ((size + 31) / 32)], size);
	}

	@Override
	BitReader bitReaderFor(BitWriter writer) {
		IntArrayBitWriter mw = (IntArrayBitWriter) writer;
		return new IntArrayBitReader(mw.getMemory(), mw.getSize());
	}
	
}
