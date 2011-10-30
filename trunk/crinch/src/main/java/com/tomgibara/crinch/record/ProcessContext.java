package com.tomgibara.crinch.record;

public interface ProcessContext {

	void setRecordsTransferred(long recordsTransferred);
	
	void setRecordCount(long recordCount);
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
