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
package com.tomgibara.crinch.record.compact;

import static com.tomgibara.crinch.record.def.ColumnType.ALL_TYPES;
import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.BYTE_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.BYTE_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.CHAR_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.CHAR_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.DOUBLE_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.DOUBLE_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.FLOAT_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.FLOAT_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.INTEGRAL_TYPES;
import static com.tomgibara.crinch.record.def.ColumnType.INT_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.INT_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.LONG_WRAPPER;
import static com.tomgibara.crinch.record.def.ColumnType.PRIMITIVE_TYPES;
import static com.tomgibara.crinch.record.def.ColumnType.SHORT_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.SHORT_WRAPPER;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ColumnParser;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.process.ProcessContext;

//TODO use column parser when appropriate methods are available

//TODO support BigInteger
//TODO support BigDecimal and identify float accuracy loss
public class RecordTyper {

	private final ColumnParser parser;
	
	private long recordCount = 0L;
	private final List<Set<ColumnType>> types = new ArrayList<Set<ColumnType>>();
	//TODO use something like this for identifying enums
	private final List<String[]> values = new ArrayList<String[]>();
	
	RecordTyper(ProcessContext context) {
		this.parser = context.getColumnParser();
	}
	
	void type(LinearRecord r) {
		int index = 0;
		while (r.hasNext()) {
			Set<ColumnType> typeSet;
			String[] valueArr;
			if (index == types.size()) {
				typeSet = EnumSet.allOf(ColumnType.class);
				valueArr = new String[2];
				types.add(typeSet);
				values.add(valueArr);
				if (recordCount == 0L) typeSet.remove(PRIMITIVE_TYPES);
			} else {
				typeSet = types.get(index);
				valueArr = values.get(index);
			}
			CharSequence chars = r.nextString();
			String str = chars == null ? "" : chars.toString(); 
			if (str.isEmpty()) {
				typeSet.removeAll(PRIMITIVE_TYPES);
			} else {
				if (typeSet.contains(LONG_WRAPPER)) {
					try {
						long value = parser.parseLong(str);
						if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
							typeSet.remove(BYTE_PRIMITIVE);
							typeSet.remove(BYTE_WRAPPER);
						}
						if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
							typeSet.remove(SHORT_PRIMITIVE);
							typeSet.remove(SHORT_WRAPPER);
						}
						if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
							typeSet.remove(INT_PRIMITIVE);
							typeSet.remove(INT_WRAPPER);
						}
					} catch (IllegalArgumentException e) {
						typeSet.removeAll(INTEGRAL_TYPES);
					}
				}
				if (typeSet.contains(FLOAT_WRAPPER)) {
					try {
						parser.parseFloat(str);
					} catch (IllegalArgumentException e) {
						typeSet.remove(FLOAT_PRIMITIVE);
						typeSet.remove(FLOAT_WRAPPER);
					}
				}
				if (typeSet.contains(DOUBLE_WRAPPER)) {
					try {
						parser.parseDouble(str);
					} catch (IllegalArgumentException e) {
						typeSet.remove(DOUBLE_PRIMITIVE);
						typeSet.remove(DOUBLE_WRAPPER);
					}
				}
				if (typeSet.contains(CHAR_WRAPPER)) {
					if (str.length() > 1) {
						typeSet.remove(CHAR_PRIMITIVE);
						typeSet.remove(CHAR_WRAPPER);
					}
				}
				if (typeSet.contains(BOOLEAN_WRAPPER)) {
					try {
						parser.parseBoolean(str);
					} catch (IllegalArgumentException e) {
						typeSet.remove(BOOLEAN_PRIMITIVE);
						typeSet.remove(BOOLEAN_WRAPPER);
					}
				}
			}
			index ++;
		}
		recordCount++;
	}

	long getRecordCount() {
		return recordCount;
	}
	
	List<ColumnType> getColumnTypes() {
		int size = types.size();
		List<ColumnType> list = new ArrayList<ColumnType>(size);
		for (int i = 0; i < size; i++) {
			Set<ColumnType> set = types.get(i);
			for (ColumnType type : ALL_TYPES) {
				if (set.contains(type)) {
					list.add(type);
					break;
				}
			}
		}
		return list;
	}
	
}
