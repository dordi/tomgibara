package com.tomgibara.crinch.record.compact;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.record.ColumnType;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordDefinition;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;

public class PositionConsumer implements RecordConsumer<LinearRecord> {

	private ProcessContext context;
	private RecordDefinition definition;
	private RecordStats stats;
	private long[] positions;
	private long[] offsets;
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		stats = context.getRecordStats();
		if (stats == null) throw new IllegalStateException("no stats");
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		definition = new RecordDefinition(true, true, types, context.getColumnOrders());
	}

	@Override
	public int getRequiredPasses() {
		return file().isFile() ? 0 : 1;
	}

	@Override
	public void beginPass() {
		//TODO support larger counts
		int count = (int) stats.getRecordCount();
		positions = new long[count];
		offsets = new long[count];
	}

	@Override
	public void consume(LinearRecord record) {
		positions[(int) record.getRecordOrdinal()] = record.getRecordPosition();
		record.exhaust();
	}

	@Override
	public void endPass() {
		calcOffsets(0, positions.length - 1);
		long errMax = 0L;
		long errSum = 0L;
		long[] errCounts = new long[64];
		for (int i = 0; i < offsets.length; i++) {
			long err = Math.abs(offsets[i]);
			if (err > errMax) errMax = err;
			errCounts[64 - Long.numberOfLeadingZeros(err)]++;
			errSum += err;
		}
		context.log("Max Err: " + errMax);
		context.log("Err Sum: " + errSum);
		context.log("Err Avg: " + errSum / (double) positions.length);
		context.log("Err Tny: " + Arrays.toString(errCounts));
	}

	private void calcOffsets(int bottom, int top) {
		if (bottom == top || bottom + 1 == top) return;
		int index = (bottom + top) / 2;
		long pos = positions[index];
		long est = (positions[bottom] + positions[top]) / 2;
		offsets[index] = pos - est;
		calcOffsets(bottom, index);
		calcOffsets(index, top);
	}
	
	@Override
	public void complete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub
		
	}

	private File file() {
		return new File(context.getOutputDir(), context.getDataName() + ".positions." + definition.getId());
	}
	
}
