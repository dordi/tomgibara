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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ColumnType {

	BOOLEAN_PRIMITIVE(boolean.class),
	BOOLEAN_WRAPPER(Boolean.class),
	BYTE_PRIMITIVE(byte.class),
	BYTE_WRAPPER(Byte.class),
	SHORT_PRIMITIVE(short.class),
	SHORT_WRAPPER(Short.class),
	INT_PRIMITIVE(int.class),
	INT_WRAPPER(Integer.class),
	LONG_PRIMITIVE(long.class),
	LONG_WRAPPER(Long.class),
	FLOAT_PRIMITIVE(float.class),
	FLOAT_WRAPPER(Float.class),
	DOUBLE_PRIMITIVE(double.class),
	DOUBLE_WRAPPER(Double.class),
	CHAR_PRIMITIVE(char.class),
	CHAR_WRAPPER(Character.class),
	STRING_OBJECT(CharSequence.class);

	public static final List<ColumnType> INTEGRAL_TYPES = Collections.unmodifiableList(Arrays.asList(
			BYTE_PRIMITIVE,
			SHORT_PRIMITIVE,
			INT_PRIMITIVE,
			LONG_PRIMITIVE,
			BYTE_WRAPPER,
			SHORT_WRAPPER,
			INT_WRAPPER,
			LONG_WRAPPER
		));

	public static final List<ColumnType> PRIMITIVE_TYPES = Collections.unmodifiableList(Arrays.asList(
			BOOLEAN_PRIMITIVE,
			BYTE_PRIMITIVE,
			SHORT_PRIMITIVE,
			INT_PRIMITIVE,
			LONG_PRIMITIVE,
			FLOAT_PRIMITIVE,
			DOUBLE_PRIMITIVE,
			CHAR_PRIMITIVE
		));
		
	public static final List<ColumnType> WRAPPER_TYPES = Collections.unmodifiableList(Arrays.asList(
			BOOLEAN_WRAPPER,
			BYTE_WRAPPER,
			SHORT_WRAPPER,
			INT_WRAPPER,
			LONG_WRAPPER,
			FLOAT_WRAPPER,
			DOUBLE_WRAPPER,
			CHAR_WRAPPER
		));
		
	public static final List<ColumnType> OBJECT_TYPES = Collections.unmodifiableList(Arrays.asList(
			STRING_OBJECT
		));

	public static final List<ColumnType> ALL_TYPES;
	
	static {
		List<ColumnType> list = new ArrayList<ColumnType>();
		list.addAll(PRIMITIVE_TYPES);
		list.addAll(WRAPPER_TYPES);
		list.addAll(OBJECT_TYPES);
		ALL_TYPES = Collections.unmodifiableList(list);
	}

	public final Class<?> typeClass;
	private final String typeName;
	
	private ColumnType(Class<?> typeClass) {
		this.typeClass = typeClass;
		String className = typeClass.getName();
		int i = className.lastIndexOf('.');
		typeName = i < 0 ? className : className.substring(i + 1);
	}
	
	@Override
	public String toString() {
		return typeName;
	}

}
