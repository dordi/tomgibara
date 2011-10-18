/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

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
	 * Flushes this output stream and forces any buffered output bits to be
	 * written out.
	 * 
	 * @throws BitStreamException
	 *             if an exception occurs flushing the stream
	 */
    
    void flush() throws BitStreamException;
    
}
