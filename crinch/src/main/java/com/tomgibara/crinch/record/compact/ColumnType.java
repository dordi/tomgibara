package com.tomgibara.crinch.record.compact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ColumnType {

	BOOLEAN_PRIMITIVE,
	BOOLEAN_WRAPPER,
	BYTE_PRIMITIVE,
	BYTE_WRAPPER,
	SHORT_PRIMITIVE,
	SHORT_WRAPPER,
	INT_PRIMITIVE,
	INT_WRAPPER,
	LONG_PRIMITIVE,
	LONG_WRAPPER,
	FLOAT_PRIMITIVE,
	FLOAT_WRAPPER,
	DOUBLE_PRIMITIVE,
	DOUBLE_WRAPPER,
	CHAR_PRIMITIVE,
	CHAR_WRAPPER,
	STRING_OBJECT;

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
	

}
