package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.RecordStats;

class RecordDecompactor {

	private final ColumnCompactor[] compactors;

	RecordDecompactor(RecordStats stats) {
		List<ColumnStats> list = stats.getColumnStats();
		int length = list.size();
		ColumnCompactor[] compactors = new ColumnCompactor[length];
		for (int i = 0; i < length; i++) {
			compactors[i] = new ColumnCompactor(list.get(i));
		}
		this.compactors = compactors;
	}

	CompactRecord decompact(CodedReader reader) {
		return new CompactRecord(compactors, reader);
	}
	
	
}
