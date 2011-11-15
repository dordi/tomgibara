package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordStats;

class RecordAnalyzer {

	private final ColumnAnalyzer[] analyzers;
	
	private long recordCount = 0;
	
	RecordAnalyzer(ProcessContext context) {
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalArgumentException("context has no column types");
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
	
	RecordStats getStats() {
		RecordStats stats = new RecordStats();
		stats.setRecordCount(recordCount);
		final int length = analyzers.length;
		List<ColumnStats> list = stats.getColumnStats();
		for (int i = 0; i < length; i++) {
			list.add( analyzers[i].stats() );
		}
		return stats;
	}
	
	@Override
	public String toString() {
		String nl = String.format("%n");
		StringBuilder sb = new StringBuilder();
		for (ColumnAnalyzer analyzer : analyzers) {
			if (sb.length() > 0) sb.append(nl);
			sb.append(analyzer.stats());
		}
		return sb.toString();
	}
	
}
