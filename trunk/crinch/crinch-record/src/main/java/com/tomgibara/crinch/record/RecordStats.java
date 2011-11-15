package com.tomgibara.crinch.record;

import java.util.ArrayList;
import java.util.List;

import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.CodedWriter;

public class RecordStats {

	public static int write(CodedWriter writer, RecordStats stats) {
		int c = writer.writePositiveLong(stats.recordCount + 1L);
		c += writer.writePositiveInt(stats.columnStats.size() + 1);
		for (ColumnStats cs : stats.columnStats) {
			c += ColumnStats.write(writer, cs);
		}
		return c;
	}

	public static RecordStats read(CodedReader reader) {
		RecordStats stats = new RecordStats();
		stats.recordCount = reader.readPositiveLong() - 1L;
		int size = reader.readPositiveInt() - 1;
		for (; size > 0; size--) {
			stats.columnStats.add(ColumnStats.read(reader));
		}
		return stats;
	}
	
	private final List<ColumnStats> columnStats = new ArrayList<ColumnStats>();
	private long recordCount;
	
	public List<ColumnStats> getColumnStats() {
		return columnStats;
	}
	
	public void setRecordCount(long recordCount) {
		if (recordCount < 0) throw new IllegalArgumentException("negative record count");
		this.recordCount = recordCount;
	}
	
	public long getRecordCount() {
		return recordCount;
	}
}
