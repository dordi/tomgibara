/*
 * Copyright 2007 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.bits;

import java.io.OutputStream;
import java.math.BigInteger;

/**
 * An interface for writing bits to a stream.
 * 
 * @author Tom Gibara
 */

public interface BitWriter {

	/**
	 * Writes the least significant bit of an int to the stream.
	 * 
	 * @param bit
	 *            the value to write
	 * 
	 * @return the number of bits written, always 1
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int writeBit(int bit) throws BitStreamException;
    
	/**
	 * Write a single bit to the stream.
	 * 
	 * @param bit
	 *            the value to write
	 * 
	 * @return the number of bits written, always 1
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */
	
    int writeBoolean(boolean bit) throws BitStreamException;
    
	/**
	 * Writes the specified number of bits to the stream.
	 * 
	 * @param count
	 *            the number of bits to write
	 * 
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    long writeBooleans(boolean value, long count) throws BitStreamException;

	/**
	 * Writes between 0 and 32 bits to the stream. Bits are read from the least
	 * significant places.
	 * 
	 * @param bits
	 *            the bits to write
	 * @param count
	 *            the number of bits to write
	 * 
	 * @return the number of bits written, always count
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int write(int bits, int count) throws BitStreamException;

	/**
	 * Writes between 0 and 64 bits to the stream. Bits are read from the least
	 * significant places.
	 * 
	 * @param bits
	 *            the bits to write
	 * @param count
	 *            the number of bits to write
	 * 
	 * @return the number of bits written, always count
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int write(long bits, int count) throws BitStreamException;

	/**
	 * Writes the specified number of bits to the stream. Bits are read from the least
	 * significant places.
	 * 
	 * @param bits
	 *            the bits to write
	 * @param count
	 *            the number of bits to write
	 * 
	 * @return the number of bits written, always count
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int write(BigInteger bits, int count) throws BitStreamException;
    
	/**
	 * Writes the supplied bits to the stream. Bits are written most-significant bits first.
	 * 
	 * @param bits
	 *            the bits to write
	 * 
	 * @return the number of bits written, always the size of the bit vector
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int write(BitVector bits) throws BitStreamException;
    
	/**
	 * Flushes this output stream and forces any buffered output bits to be
	 * written out to an underlying stream. This does not necessarily cause an
	 * underlying stream to flush should that operation is supported (eg. if
	 * bits are being written to an {@link OutputStream}).
	 * 
	 * NOTE: Implementations that write bits to an underlying medium that cannot
	 * persist individual bits (eg files) may necessarily retain some number of
	 * bits that cannot be persisted until a boundary has been reached.
	 * 
	 * @throws BitStreamException
	 *             if an exception occurs flushing the stream
	 * @see #padToBoundary(BitBoundary)
	 */
    
    void flush() throws BitStreamException;
    
	/**
	 * Pads the stream with zeros up to the specified boundary. If the stream is
	 * already positioned on a boundary, zero bits will be written.
	 * 
	 * It may be necessary to call this method prior to {@link #flush()} on some
	 * {@link BitWriter} implementations.
	 * 
	 * NOTE: This method may not be supported by writers that cannot track their
	 * position in the bit stream.
	 * 
	 * @param boundary
	 *            the 'size' of boundary
	 * @return the number of zero bits written to the stream
	 * @throws UnsupportedOperationException
	 *             if the stream does not support padding
	 * @throws BitStreamException
	 *             if an exception occurs when padding
	 */
    
    int padToBoundary(BitBoundary boundary) throws UnsupportedOperationException, BitStreamException;

	/**
	 * The position of the writer in the stream; usually, but not necessarily,
	 * the number of bits written. Implementations that cannot report their
	 * position should consistently return -1L.
	 * 
	 * @return the position in the stream, or -1L
	 */
    
    long getPosition();
}
