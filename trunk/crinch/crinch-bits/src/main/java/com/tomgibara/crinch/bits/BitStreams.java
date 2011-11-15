package com.tomgibara.crinch.bits;

public class BitStreams {

	private BitStreams() {}
	
	public static boolean isSameBits(BitReader r, BitReader s) {
		int rBit;
		int sBit;
		do {
			try {
				rBit = r.readBit();
			} catch (EndOfBitStreamException e) {
				rBit = -1;
			}
			try {
				sBit = s.readBit();
			} catch (EndOfBitStreamException e) {
				sBit = -1;
			}
			if (rBit != sBit) return false;
		} while (rBit != -1);
		return true;
	}
	
	public static String bitsToString(BitReader reader) {
		StringBuilder sb = new StringBuilder();
		while (true) {
			try {
				sb.append(reader.readBoolean() ? '1' : '0');
			} catch (EndOfBitStreamException e) {
				return sb.toString();
			}
		}
	}
	
	public static long countBits(BitReader reader) {
		return reader.skipBits(Long.MAX_VALUE);
	}
	
}
