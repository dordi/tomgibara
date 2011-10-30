package com.tomgibara.crinch.record;

import java.util.List;

public interface ProcessContext {

	void setRecordsTransferred(long recordsTransferred);
	
	void setRecordCount(long recordCount);
	
	void setPassName(String passName);
	
	String getPassName();
	
	void setColumnStats(List<ColumnStats> columnStats);
	
	List<ColumnStats> getColumnStats();
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
