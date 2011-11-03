package com.tomgibara.crinch.record;

import java.util.List;

import com.tomgibara.crinch.coding.ExtendedCoding;


public interface ProcessContext {

	void setCoding(ExtendedCoding coding);
	
	ExtendedCoding getCoding();
	
	void setRecordsTransferred(long recordsTransferred);
	
	void setPassName(String passName);
	
	String getPassName();
	
	void setRecordStats(RecordStats recordStats);
	
	RecordStats getRecordStats();
	
	void setColumnTypes(List<ColumnType> columnTypes);
	
	List<ColumnType> getColumnTypes();
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
