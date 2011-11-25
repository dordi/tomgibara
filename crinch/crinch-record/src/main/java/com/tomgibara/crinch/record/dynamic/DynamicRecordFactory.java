package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.LONG_PRIMITIVE;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.codehaus.janino.SimpleCompiler;

import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.def.ColumnDefinition;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDefinition;

//TODO could persist nullables as primitives
//TODO currently limited to 32768 fields
public class DynamicRecordFactory {

	
	private static String packageName = "com.tomgibara.crinch.record.dynamic";
	
	private static final Map<String, DynamicRecordFactory> factories = new HashMap<String, DynamicRecordFactory>();
	
	private static final Class<?>[] consParams = { LinearRecord.class, boolean.class };

	//TODO should be tackled at the type level
	private static String accessorName(ColumnType type) {
		switch (type) {
		case CHAR_WRAPPER: return "Char";
		case INT_WRAPPER: return "Int";
		default:
			return Character.toUpperCase(type.toString().charAt(0)) + type.toString().substring(1);
		}
	}
	
	public static DynamicRecordFactory getInstance(RecordDefinition definition) {
		String name = "DynRec_" + definition.getId();
		DynamicRecordFactory factory;
		synchronized (factories) {
			factory = factories.get(name);
			if (factory == null) {
				factory = new DynamicRecordFactory(definition, name);
				factories.put(name, factory);
			}
		}
		return factory; 
	}
	
	// fields
	
	private final RecordDefinition definition;
	private final String name;
	private final String source;
	private final Class<? extends LinearRecord> clss;
	private final Constructor<? extends LinearRecord> cons;
	
	// constructors
	
	private DynamicRecordFactory(RecordDefinition definition, String name) {
		this.definition = definition;
		this.name = name;
		source = generateSource();
		//TODO don't want to accumulate ClassLoaders!
		SimpleCompiler compiler = new SimpleCompiler();
		try {
			compiler.cook(generateSource());
		} catch (CompileException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} catch (ScanException e) {
			throw new RuntimeException(e);
		}
		try {
			clss = (Class) compiler.getClassLoader().loadClass(packageName + "." + name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		try {
			cons = clss.getConstructor(consParams);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public RecordDefinition getDefinition() {
		return definition;
	}
	
	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public LinearRecord newRecord(LinearRecord record) {
		return newRecord(record, false);
	}

	public LinearRecord newRecord(LinearRecord record, boolean basis) {
		try {
			return cons.newInstance(record, basis);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String generateSource() {
		//TODO make configurable
		boolean markable = true;
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(packageName).append(";\n");
		sb.append("public class " + name).append(" implements " + LinearRecord.class.getName() + ", Comparable {\n");
		sb.append("\tprivate static final short limit = ").append(definition.getTypes().size()).append(";\n");
		
		// fields
		if (definition.isOrdinal()) sb.append("\tprivate final long recordOrdinal;\n");
		if (definition.isPositional()) sb.append("\tprivate final long recordPosition;\n");
		sb.append("\tprivate short field = 0;\n");
		if (markable) sb.append("\tprivate short mark = Short.MAX_VALUE;\n");
		{
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				sb.append("\tprivate ");
				if (!markable) sb.append("final ");
				sb.append(type).append(" f_").append(field++).append(";\n");
			}
		}
		
		// constructors
		{
			sb.append("\tpublic ").append(name).append("() {\n");
			sb.append("\t}\n");
		}
		
		{
			sb.append("\tpublic ").append(name).append("(" + LinearRecord.class.getName() + " record, boolean basis) {\n");
			if (definition.isOrdinal()) sb.append("\t\tthis.recordOrdinal = record.getRecordOrdinal();\n");
			if (definition.isPositional()) sb.append("\t\tthis.recordPosition = record.getRecordPosition();\n");
			sb.append("\t\tif (basis) {\n");
			if (definition.getBasis() == null) {
				sb.append("\t\t\tthrow new IllegalArgumentException(\"no basis defined\");\n");
			} else {
				generateRecordCopy(sb, definition.getBasisColumns());
			}
			sb.append("\t\t} else {\n");
			generateRecordCopy(sb, definition.getColumns());
			sb.append("\t\t}\n");
			sb.append("\t}\n");
		}
		
		// accessors
		sb.append("\tpublic long getRecordOrdinal() {\n");
		sb.append("\t\treturn ").append(definition.isOrdinal() ? "recordOrdinal" : "-1L").append(";\n");
		sb.append("\t}\n");

		sb.append("\tpublic long getRecordPosition() {\n");
		sb.append("\t\treturn ").append(definition.isPositional() ? "recordPosition" : "-1L").append(";\n");
		sb.append("\t}\n");

		// next methods
		List<ColumnType> types = new ArrayList<ColumnType>();
		types.addAll(ColumnType.PRIMITIVE_TYPES);
		types.addAll(ColumnType.OBJECT_TYPES);
		for (ColumnType type : types) {
			sb.append("\tpublic ").append(type).append(" next").append(Character.toUpperCase(type.toString().charAt(0))).append(type.toString().substring(1)).append("() {\n");
			sb.append("\t\tif (field == limit) throw new IllegalStateException(\"fields exhausted\");\n");
			sb.append("\t\tswitch(field++) {\n");
			int field = 0;
			for (ColumnType t : definition.getTypes()) {
				if (type == t) {
					sb.append("\t\t\tcase ").append(field).append(":\n");
					if (markable && type.typeClass.isPrimitive()) {
						sb.append("\t\t\treturn f_").append(field).append(";\n");
					} else {
						sb.append("\t\t\t").append(type).append(" tmp_").append(field).append(" = f_").append(field).append(";\n");
						sb.append("\t\t\tf_").append(field).append(" = null;\n");
						sb.append("\t\t\treturn tmp_").append(field).append(";\n");
					}
				}
				field++;
			}
			sb.append("\t\tdefault:\n");
			sb.append("\t\t\tfield--;\n");
			sb.append("\t\t\tthrow new IllegalStateException(\"Field \" + field + \" not of type ").append(type).append("\");\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n");
		}
		
		// other linear record methods
		sb.append("\tpublic boolean hasNext() { return field != limit; }\n");
		
		sb.append("\tpublic void skipNext() {\n");
		sb.append("\t\tif (field == limit) throw new IllegalStateException(\"fields exhausted\");\n");
		sb.append("\t\tfield++;\n");
		sb.append("\t}\n");
		
		sb.append("\tpublic boolean wasInvalid() {\n");
		sb.append("\t\tif (field == 0) throw new IllegalStateException(\"no field read\");\n");
		sb.append("\t\treturn false;\n");
		sb.append("\t}\n");
		
		{
			sb.append("\tpublic boolean wasNull() {\n");
			sb.append("\t\tswitch(field) {\n");
			sb.append("\t\t\tcase 0: throw new IllegalStateException(\"no field read\");\n");
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				sb.append("\t\t\tcase ").append(field + 1).append(": ");
				if (type.typeClass.isPrimitive()) {
					sb.append("return false;");
				} else {
					sb.append("return f_" + field + " == null;");
				}
				field++;
				sb.append('\n');
			}
			sb.append("\t\tdefault: throw new IllegalStateException(\"fields exhausted\");\n");
			sb.append("\t\t}\n");
			sb.append("\t}\n");
		}
		
		sb.append("\tpublic void mark() {\n");
		if (markable) {
			sb.append("\t\tmark = field;\n");
		} else {
			sb.append("\t\tthrow new UnsupportedOperationException(\"mark not supported\")");
		}
		sb.append("\t}\n");
		
		sb.append("\tpublic void reset() {\n");
		if (markable) {
			sb.append("\t\tif (mark > limit) throw new IllegalStateException(\"not marked\");\n");
			sb.append("\t\tfield = mark;\n");
		} else {
			sb.append("\t\tthrow new UnsupportedOperationException(\"mark not supported\");\n");
		}
		sb.append("\t}\n");
		
		sb.append("\tpublic void exhaust() { field = limit; }\n");

		// comparable methods
		{
			sb.append("\tpublic int compareTo(Object obj) {\n");
			sb.append("\t\t" + name + " that = (" + name + ") obj;\n");
			for (ColumnDefinition column : definition.getOrderedColumns()) {
				int field = column.getIndex();
				ColumnOrder order = column.getOrder();
				ColumnType type = definition.getTypes().get(field);
				if (type == BOOLEAN_PRIMITIVE) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " ? -1 : 1;\n");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " " + (order.isAscending() ? '<' : '>') + " that.f_" + field + " ? -1 : 1;\n");
				} else {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") {\n");
					sb.append("\t\t\tif (this.f_" + field + " == null) return " + (order.isNullFirst() ? "-1" : "1") + ";\n");
					sb.append("\t\t\tif (that.f_" + field + " == null) return " + (order.isNullFirst() ? "1" : "-1") + ";\n");
					sb.append("\t\t\treturn " + (order.isAscending() ? "this" : "that") + ".f_" + field + ".compareTo(" + (order.isAscending() ? "that" : "this") + ".f_" + field + ");\n");
					sb.append("\t\t}\n");
				}
			}
			sb.append("\t\treturn 0;\n");
			sb.append("\t}\n");
		}
		
		// object methods
		{
			sb.append("\tpublic int hashCode() {\n");
			
			sb.append("\t\tint h = 0;\n");
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				if (type == BOOLEAN_PRIMITIVE) {
					sb.append("\t\th = (31 * h) ^ (f_" + field + " ? 1231 : 1237);\n");
				} else if (type == LONG_PRIMITIVE) {
					sb.append("\t\th = (31 * h) ^ (int) (f_" + field + " >> 32) ^ (int) f_" + field + ";\n");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\th = (31 * h) ^ f_" + field + ";\n");
				} else {
					
				}
				field++;
			}
			sb.append("\t\treturn h;\n");
			sb.append("\t}\n");
		}

		{
			sb.append("\tpublic boolean equals(Object obj) {\n");
			sb.append("\t\tif (obj == this) return true;\n");
			sb.append("\t\tif (!(obj instanceof " + name +")) return false;\n");
			sb.append("\t\t" + name + " that = (" + name + ") obj;\n");
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				if (type.typeClass.isPrimitive()) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return false;\n");
				} else {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") {\n");
					sb.append("\t\t\tif (this.f_" + field + " == null || that.f_" + field + " == null) return false;\n");
					sb.append("\t\t\tif (!this.f_" + field + ".equals(that.f_" + field + ")) return false;\n");
					sb.append("\t\t}\n");
				}
				field++;
			}
			sb.append("\t\treturn true;\n");
			sb.append("\t}\n");
		}

		{
			sb.append("\tpublic String toString() {\n");
			
			sb.append("\t\treturn new StringBuilder().append('[')");
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				if (field > 0) sb.append(".append(',')");
				sb.append(".append(f_" + field + ")");
				field++;
			}
			sb.append(".append(']').toString();\n");
			sb.append("\t}\n");
		}

		sb.append("}");
		return sb.toString();
	}

	private void generateRecordCopy(StringBuilder sb, List<ColumnDefinition> columns) {
		for (ColumnDefinition column : columns) {
			if (column == null) {
				sb.append("\t\t\trecord.skipNext();\n");
			} else {
				int field = column.getIndex();
				ColumnType type = column.getType();
				String accessorName = accessorName(type);
				if (type.typeClass.isPrimitive()) {
					sb.append("\t\t\tf_").append(field).append(" = record.next").append(accessorName).append("();\n");
				} else {
					sb.append("\t\t\t").append(type).append(" tmp_").append(field).append(" = record.next").append(accessorName).append("();\n");
					sb.append("\t\t\tf_").append(field).append(" = record.wasNull() ? null : tmp_").append(field).append(";\n");
				}
			}
		}
	}

}
