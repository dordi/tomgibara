package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ValueParser;

class RecordAnalyzer {

	private final ValueParser parser;
	private final ColumnAnalyzer[] analyzers;
	
	private long recordCount = 0;
	
	RecordAnalyzer(ValueParser parser, List<ColumnType> types) {
		this.parser = parser;
		analyzers = new ColumnAnalyzer[types.size()];
		for (int i = 0; i < analyzers.length; i++) {
			analyzers[i] = ColumnAnalyzer.newInstance(types.get(i));
		}
	}
	
	void analyze(LinearRecord record) {
		recordCount++;
		for (int i = 0; i < analyzers.length; i++) {
			analyzers[i].analyze(record.nextString());
		}
	}
	
	long getRecordCount() {
		return recordCount;
	}
	
	RecordCompactor compactor() {
		ColumnCompactor[] compactors = new ColumnCompactor[analyzers.length];
		for (int i = 0; i < compactors.length; i++) {
			compactors[i] = analyzers[i].compactor();
		}
		return new RecordCompactor(parser, compactors);
	}

	@Override
	public String toString() {
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder();
		for (ColumnAnalyzer analyzer : analyzers) {
			if (sb.length() > 0) sb.append(nl);
			sb.append(analyzer.compactor());
		}
		return sb.toString();
	}
	
}
