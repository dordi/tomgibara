package com.tomgibara.crinch.record;

public interface ProcessContext {

	void setProgress(long progress);
	
	void setProgressScale(long progressScale);
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
