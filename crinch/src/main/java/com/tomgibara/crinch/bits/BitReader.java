/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

/**
 * An interface for reading bits from a stream.
 * 
 * @author Tom Gibara
 *
 */

public interface BitReader {

	/**
	 * Reads a single bit from a stream of bits.
	 * 
	 * @return the value 0 or 1
	 * @throws BitStreamException
	 *             if an exception occurs when reading the stream
	 */
	
    int readBit() throws BitStreamException;
    
    /**
     * Reads a single bit from a stream of bits.
     * 
     * @return whether the bit was set
	 * @throws BitStreamException
	 *             if an exception occurs when reading the stream
     */
    
    boolean readBoolean() throws BitStreamException;
    
	/**
	 * Read between 0 and 32 bits from a stream of bits. Bits are returned in
	 * the least significant places.
	 * 
	 * @param count
	 *            the number of bits to read
	 * @return the read bits
	 * @throws BitStreamException
	 *             if an exception occurs when reading the stream
	 */
    
    int read(int count) throws BitStreamException;
    
	/**
	 * Read between 0 and 64 bits from a stream of bits. Bits are returned in
	 * the least significant places.
	 * 
	 * @param count
	 *            the number of bits to read
	 * @return the read bits
	 * @throws BitStreamException
	 *             if an exception occurs when reading the stream
	 */
    
    long readLong(int count) throws BitStreamException;
}
