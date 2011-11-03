package com.tomgibara.crinch.record;

import java.util.List;

import com.tomgibara.crinch.coding.EliasOmegaCoding;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.compact.ColumnType;

public class StdProcessContext implements ProcessContext {

	private float progressStep = 1.0f;
	private ExtendedCoding coding = EliasOmegaCoding.extended;
	
	private long recordCount = 0L;
	private long recordsTransferred = 0L;
	private float progress;
	private float lastProgress;
	private String passName;
	private RecordStats recordStats;
	private List<ColumnType> columnTypes;

	public StdProcessContext() {
		resetProgress();
	}

	public void setProgressStep(float progressStep) {
		if (progressStep < 0f) throw new IllegalArgumentException("negative progress");
		this.progressStep = progressStep;
	}

	@Override
	public void setCoding(ExtendedCoding coding) {
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.coding = coding;
	}

	@Override
	public ExtendedCoding getCoding() {
		return coding;
	}
	
	@Override
	public void setRecordsTransferred(long recordsTransferred) {
		if (recordCount == 0f) return;
		float progress;
		if (recordsTransferred <= 0) {
			progress = 0f;
		} else if (recordsTransferred >= recordCount) {
			progress = 1f;
		} else {
			progress = (float)(recordsTransferred / (double) recordCount);
		}
		if (progress < this.progress || progress > lastProgress + progressStep || progress > lastProgress && progress == 1f) {
			reportProgress(progress);
		}
		this.recordsTransferred = recordsTransferred;
	}
	
	@Override
	public void setPassName(String passName) {
		if (passName != null && !passName.equals(this.passName)) {
			log("Pass: " + passName);
		}
		this.passName = passName;
	}
	
	@Override
	public String getPassName() {
		return passName;
	}
	
	@Override
	public void setRecordStats(RecordStats recordStats) {
		if (recordStats != null && !recordStats.equals(this.recordStats)) {
			int col = 1;
			for (ColumnStats stats : recordStats.getColumnStats()) {
				log("Statistics - column " + col++ + ": " + stats);
			}
			setRecordCount(recordStats.getRecordCount());
		}
		this.recordStats = recordStats;
		
	}
	
	@Override
	public RecordStats getRecordStats() {
		return recordStats;
	}

	@Override
	public void setColumnTypes(List<ColumnType> columnTypes) {
		if (columnTypes != null && !columnTypes.equals(this.columnTypes)) {
			int col = 1;
			for (ColumnType type : columnTypes) {
				log("Type - column " + col++ + ": " + type);
			}
		}
		this.columnTypes = columnTypes;
	}

	@Override
	public List<ColumnType> getColumnTypes() {
		return columnTypes;
	}
	
	@Override
	public void log(String message) {
		System.out.println(message);
	}
	
	@Override
	public void log(String message, Throwable t) {
		System.err.println(message + "(records transferred: " + recordsTransferred + ")");
		t.printStackTrace();
	}

	private void setRecordCount(long recordCount) {
		if (recordCount < 0L) throw new IllegalArgumentException("negative recordCount");
		if (recordCount == this.recordCount) return;
		this.recordCount = recordCount;
		recordsTransferred = Math.min(recordsTransferred, recordCount);
		log("Record count: " + recordCount);
		if (recordCount == 0L) resetProgress();
	}
	
	private void resetProgress() {
		progress = 0f;
		lastProgress = -1f;
	}
	
	private void reportProgress(float progress) {
		if (progressStep >= 1.0f) return;
		this.progress = progress;
		lastProgress = progress;
		log("Progress: " + Math.round(progress * 100f) + "%");
	}
	
}
