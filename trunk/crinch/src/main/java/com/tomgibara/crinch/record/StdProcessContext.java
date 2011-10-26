package com.tomgibara.crinch.record;

public class StdProcessContext implements ProcessContext {

	private float progressStep = 0.01f;
	private long progressScale = 0L;
	private float lastProgress;
	private float progress;

	public StdProcessContext() {
		resetProgress();
	}
	
	public void setProgressStep(float progressStep) {
		if (progressStep < 0f) throw new IllegalArgumentException("negative progress");
		this.progressStep = progressStep;
	}
	
	@Override
	public void setProgress(long progress) {
		if (progressScale == 0f) return;
		float p;
		if (progress <= 0) {
			p = 0f;
		} else if (progress >= progressScale) {
			p = 1f;
		} else {
			p = (float)(progress / (double) progressScale);
		}
		if (p < this.progress || p > lastProgress + progressStep || p > lastProgress && p == 1f) {
			reportProgress(p);
		}
	}
	
	@Override
	public void setProgressScale(long progressScale) {
		if (progressScale < 0L) throw new IllegalArgumentException("negative progress");
		this.progressScale = progressScale;
		if (progressScale == 0L) resetProgress();
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
