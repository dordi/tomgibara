/*
 * Copyright 2011 Tom Gibara
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

import java.io.PrintStream;
import java.math.BigInteger;

public class ProfiledBitReader implements BitReader {

	private static final int GP = 0;
	private static final int SP = 1;
	private static final int R = 2;
	private static final int RBI = 3;
	private static final int RB = 4;
	private static final int RBS = 5;
	private static final int RZ = 6;
	private static final int RL = 7;
	private static final int RU = 8;
	private static final int SB = 9;
	private static final int STB = 10;
	
	private final BitReader reader;
	private final long[] calls = new long[9];
	
	public ProfiledBitReader(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		this.reader = reader;
	}

	@Override
	public long getPosition() {
		calls[GP]++;
		return reader.getPosition();
	}

	@Override
	public long setPosition(long newPosition) throws IllegalArgumentException {
		calls[SP]++;
		return reader.setPosition(newPosition);
	}
	
	@Override
	public int read(int count) throws BitStreamException {
		calls[R]++;
		return reader.read(count);
	}

	@Override
	public BigInteger readBigInt(int count) throws BitStreamException {
		calls[RBI]++;
		return reader.readBigInt(count);
	}

	@Override
	public int readBit() throws BitStreamException {
		calls[RB]++;
		return reader.readBit();
	}

	@Override
	public void readBits(BitVector bits) throws BitStreamException {
		calls[RBS]++;
		reader.readBits(bits);
	}

	@Override
	public boolean readBoolean() throws BitStreamException {
		calls[RZ]++;
		return reader.readBoolean();
	}

	@Override
	public long readLong(int count) throws BitStreamException {
		calls[RL]++;
		return reader.readLong(count);
	}

	@Override
	public int readUntil(boolean one) {
		calls[RU]++;
		return reader.readUntil(one);
	}
	
	@Override
	public long skipBits(long count) {
		calls[SB]++;
		return reader.skipBits(count);
	}

	@Override
	public int skipToBoundary(BitBoundary boundary) {
		calls[STB]++;
		return reader.skipToBoundary(boundary);
	}

	public void dumpProfile(PrintStream out) {
		dump(out, "getPositionInStream", 0);
		dump(out, "read", 1);
		dump(out, "readBigInt", 2);
		dump(out, "readBit", 3);
		dump(out, "readBits", 4);
		dump(out, "readBoolean", 5);
		dump(out, "readLong", 6);
		dump(out, "skipBits", 7);
		dump(out, "skipToBoundary", 8);
	}
	
	private void dump(PrintStream out, String label, int i) {
		out.print(label);
		out.print(": ");
		out.print(calls[i]);
		out.print("\n");
	}
	
}
