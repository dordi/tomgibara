package com.tomgibara.crinch.record;

import java.util.List;

public class StdProcessContext implements ProcessContext {

	private float progressStep = 0.05f;
	private long recordCount = 0L;
	private float progress;
	private float lastProgress;
	private List<ColumnStats> columnStats;

	public StdProcessContext() {
		resetProgress();
	}
	
	public void setProgressStep(float progressStep) {
		if (progressStep < 0f) throw new IllegalArgumentException("negative progress");
		this.progressStep = progressStep;
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
	}
	
	@Override
	public void setRecordCount(long recordCount) {
		if (recordCount < 0L) throw new IllegalArgumentException("negative progress");
		this.recordCount = recordCount;
		if (recordCount == 0L) resetProgress();
	}
	
	public void setColumnStats(List<ColumnStats> columnStats) {
		this.columnStats = columnStats;
	}
	
	public List<ColumnStats> getColumnStats() {
		return columnStats;
	}
	
	@Override
	public void log(String message) {
		System.out.println(message);
	}
	
	@Override
	public void log(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace();
	}

	private void resetProgress() {
		progress = 0f;
		lastProgress = -1f;
	}
	
	private void reportProgress(float progress) {
		this.progress = progress;
		lastProgress = progress;
		log("Progress: " + Math.round(progress * 100f) + "%");
	}
	
}
