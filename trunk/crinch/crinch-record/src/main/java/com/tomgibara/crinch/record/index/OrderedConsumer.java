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
package com.tomgibara.crinch.record.index;

import java.io.File;
import java.util.List;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.process.ProcessContext;

public abstract class OrderedConsumer implements RecordConsumer<LinearRecord> {

	private final SubRecordDef subRecDef;
	
	ProcessContext context;
	RecordDef definition;
	DynamicRecordFactory factory;

	public OrderedConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalArgumentException("no record definition");
		definition = def.asBasis();
		if (subRecDef != null) definition = definition.asSubRecord(subRecDef);
		factory = DynamicRecordFactory.getInstance(definition);
	}

	File sortedFile(boolean input) {
		return context.file("compact", false, input ? definition.getBasis() : definition);
	}
	

}
