/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.coding;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.crinch.bits.BitReader;

public class CodedReader {

	// fields
	
	private final BitReader reader;
	private final ExtendedCoding coding;
	
	// constructors
	
	public CodedReader(BitReader reader, ExtendedCoding coding) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.reader = reader;
		this.coding = coding;
	}
	
	// accessors
	
	public BitReader getReader() {
		return reader;
	}
	
	public ExtendedCoding getCoding() {
		return coding;
	}
	
	// methods
	
	public int readPositiveInt() {
		return coding.decodePositiveInt(reader);
	}
	
	public long readPositiveLong() {
		return coding.decodePositiveLong(reader);
	}
	
	public BigInteger readPositiveBigInt() {
		return coding.decodePositiveBigInt(reader);
	}
	
	public int readSignedInt() {
		return coding.decodeSignedInt(reader);
	}
	
	public long readSignedLong() {
		return coding.decodeSignedLong(reader);
	}
	
	public BigInteger readSignedBigInt() {
		return coding.decodeSignedBigInt(reader);
	}
	
	public double readDouble() {
		return coding.decodeDouble(reader);
	}
	
	public BigDecimal readDecimal() {
		return coding.decodeDecimal(reader);
	}
	
}
