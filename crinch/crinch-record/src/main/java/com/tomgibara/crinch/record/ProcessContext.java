/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;


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
	
	void setRecordCount(long recordCount);
	
	long getRecordCount();
	
	void setRecordStats(RecordStats recordStats);
	
	RecordStats getRecordStats();
	
	void setColumnTypes(List<ColumnType> columnTypes);
	
	List<ColumnType> getColumnTypes();
	
	void setColumnOrders(List<ColumnOrder> columnOrders);
	
	List<ColumnOrder> getColumnOrders();

	RecordDef getRecordDef();
	
	void log(String message);
	
	void log(String message, Throwable t);

	File file(String type, boolean stats, RecordDef def);
}
