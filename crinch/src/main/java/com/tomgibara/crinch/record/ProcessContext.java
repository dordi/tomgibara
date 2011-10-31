package com.tomgibara.crinch.record;

import com.tomgibara.crinch.coding.ExtendedCoding;


public interface ProcessContext {

	void setCoding(ExtendedCoding coding);
	
	ExtendedCoding getCoding();
	
	void setRecordsTransferred(long recordsTransferred);
	
	//TODO look at eliminating this redundant field
	void setRecordCount(long recordCount);
	
	void setPassName(String passName);
	
	String getPassName();
	
	void setRecordStats(RecordStats recordStats);
	
	RecordStats getRecordStats();
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
