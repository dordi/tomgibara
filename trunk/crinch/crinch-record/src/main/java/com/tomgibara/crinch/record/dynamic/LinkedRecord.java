package com.tomgibara.crinch.record.dynamic;

import com.tomgibara.crinch.record.LinearRecord;

public interface LinkedRecord extends LinearRecord {

	void insertRecordBefore(LinkedRecord record);
	
	void insertRecordAfter(LinkedRecord record);
	
	LinkedRecord getNextRecord();
	
	LinkedRecord getPreviousRecord();
	
	void removeRecord();
	
	void replaceRecord(LinkedRecord record);
	
}
