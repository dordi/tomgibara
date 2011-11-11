/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

import java.math.BigInteger;

/**
 * An interface for writing bits to a stream.
 * 
 * @author Tom Gibara
 */

public interface BitWriter {

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
	 * Writes the specified number of zero bits to the stream.
	 * 
	 * @param count
	 *            the number of zero bits to write
	 * 
	 * @return the number of bits written, always count
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int writeZeros(int count) throws BitStreamException;

	/**
	 * Writes the specified number of one bits to the stream.
	 * 
	 * @param count
	 *            the number of one bits to write
	 * 
	 * @return the number of bits written, always count
	 * @throws BitStreamException
	 *             if an exception occurs when writing to the stream
	 */

    int writeOnes(int count) throws BitStreamException;
    
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
	 * written out.
	 * 
	 * @throws BitStreamException
	 *             if an exception occurs flushing the stream
	 */
    
    void flush() throws BitStreamException;
    
	/**
	 * Pads the stream with zeros upto the specified boundary. If the stream is
	 * already positioned on a boundary, zero bits will be written.
	 * 
	 * @param boundary
	 *            the 'size' of boundary
	 * @return the number of zero bits written to the stream
	 * @throws UnsupportedOperationException
	 *             if the stream does not support alignment
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
