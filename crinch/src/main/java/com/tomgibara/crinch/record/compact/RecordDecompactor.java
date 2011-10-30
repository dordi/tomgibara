package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.EliasOmegaEncoding;
import com.tomgibara.crinch.record.ColumnStats;

class RecordDecompactor {

	static int write(RecordDecompactor rc, BitWriter writer) {
		ColumnCompactor[] compactors = rc.compactors;
		int count = compactors.length;
		int c = EliasOmegaEncoding.encodePositiveInt(count + 1, writer);
		for (int i = 0; i < count; i++) {
			c += ColumnStats.write(compactors[i].getStats(), writer);
		}
		return c;
	}
	
	static RecordDecompactor read(BitReader reader) {
		int length = EliasOmegaEncoding.decodePositiveInt(reader) - 1;
		ColumnCompactor[] compactors = new ColumnCompactor[length];
		for (int i = 0; i < length; i++) {
			compactors[i] = new ColumnCompactor(ColumnStats.read(reader));
		}
		return new RecordDecompactor(compactors);
	}

	private final ColumnCompactor[] compactors;

	RecordDecompactor(ColumnCompactor[] compactors) {
		this.compactors = compactors;
	}

	CompactRecord decompact(BitReader reader) {
		return new CompactRecord(compactors, reader);
	}
	
	
}
