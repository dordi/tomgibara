package com.tomgibara.crinch.record.compact;

import com.tomgibara.crinch.record.AbstractConsumer;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.process.ProcessContext;

public class StatsConsumer extends AbstractConsumer<LinearRecord> {

	private ProcessContext context = null;
	private RecordAnalyzer analyzer = null;

	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
	}

	@Override
	public int getRequiredPasses() {
		RecordStats stats = context.getRecordStats();
		//TODO empty check is a kludge - ideally record count should be stored separately?
		return stats == null || stats.getColumnStats().isEmpty() ? 1 : 0;
	}
	
	@Override
	public void beginPass() {
		if (analyzer == null) {
			analyzer = new RecordAnalyzer(context);
			context.setPassName("Gathering statistics");
		} else {
			context.setPassName("Identifying unique values");
		}
		analyzer.beginAnalysis();
	}
	
	@Override
	public void consume(LinearRecord record) {
		analyzer.analyze(record);
	}
	
	@Override
	public void endPass() {
		analyzer.endAnalysis();
		if (!analyzer.needsReanalysis()) {
			context.setRecordStats(analyzer.getStats());
		}
	}

}
