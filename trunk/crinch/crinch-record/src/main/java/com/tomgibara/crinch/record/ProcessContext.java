package com.tomgibara.crinch.record;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.coding.ExtendedCoding;


public interface ProcessContext {

	void setCoding(ExtendedCoding coding);
	
	ExtendedCoding getCoding();

	void setClean(boolean clean);
	
	boolean isClean();
	
	void setOutputDir(File outputDir);
	
	File getOutputDir();
	
	void setDataName(String dataName);
	
	String getDataName();
	
	void setColumnParser(ColumnParser columnParser);
	
	ColumnParser getColumnParser();
	
	void setRecordsTransferred(long recordsTransferred);
	
	void setPassName(String passName);
	
	String getPassName();
	
	void setRecordStats(RecordStats recordStats);
	
	RecordStats getRecordStats();
	
	void setColumnTypes(List<ColumnType> columnTypes);
	
	List<ColumnType> getColumnTypes();
	
	void setColumnOrders(List<ColumnOrder> columnOrders);
	
	List<ColumnOrder> getColumnOrders();
	
	void log(String message);
	
	void log(String message, Throwable t);

	
}
