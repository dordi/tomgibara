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
package com.tomgibara.crinch.record.def;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.util.WriteStream;

public class ColumnDef {

	// statics
	
	static HashSource<ColumnDef> hashSource = new HashSource<ColumnDef>() {
		
		@Override
		public void sourceData(ColumnDef definition, WriteStream out) {
			out.writeInt(definition.index);
			out.writeInt(definition.type.ordinal());
			if (definition.order != null) {
				ColumnOrder.hashSource.sourceData(definition.order, out);
			}
		}
	};

	
	// fields
	
	private final int index;
	private final ColumnType type;
	private final ColumnOrder order;
	private final int basis;

	// constructors
	
	ColumnDef(int index, ColumnType type, ColumnOrder order, int basis) {
		this.index = index;
		this.type = type;
		this.order = order;
		this.basis = basis;
	}
	
	// accessors
	
	public int getIndex() {
		return index;
	}
	
	public ColumnType getType() {
		return type;
	}
	
	public ColumnOrder getOrder() {
		return order;
	}
	
	int getBasis() {
		return basis;
	}
	
	//TODO implement object methods

	@Override
	public String toString() {
		return "index: " + index + ", type: " + type + ", order: " + order;
	}
	
}
