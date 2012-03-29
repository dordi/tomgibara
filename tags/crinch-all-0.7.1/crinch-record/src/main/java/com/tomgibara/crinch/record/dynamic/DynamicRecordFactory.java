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
package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.def.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.DOUBLE_PRIMITIVE;
import static com.tomgibara.crinch.record.def.ColumnType.FLOAT_PRIMITIVE;
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

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessScoped;
import com.tomgibara.crinch.record.def.ColumnDef;
import com.tomgibara.crinch.record.def.ColumnOrder;
import com.tomgibara.crinch.record.def.ColumnOrder.Sort;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.util.WriteStream;

//TODO could persist nullables as primitives
//TODO currently limited to 32768 fields
//TODO basing different configurations on a single base class would be preferable
public class DynamicRecordFactory {

	private static String packageName = "com.tomgibara.crinch.record.dynamic";
	
	private static final Map<String, DynamicRecordFactory> factories = new HashMap<String, DynamicRecordFactory>();
	
	private static final Class<?>[] consParams = { LinearRecord.class, boolean.class };

	//TODO should be tackled at the type level
	private static String accessorName(ColumnType type) {
		switch (type) {
		case CHAR_WRAPPER: return "Char";
		case INT_WRAPPER: return "Int";
		case STRING_OBJECT: return "String";
		default:
			return Character.toUpperCase(type.toString().charAt(0)) + type.toString().substring(1);
		}
	}
	
	public static DynamicRecordFactory getInstance(RecordDef definition) {
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
	
	public final static class ClassConfig {
		
		static final ClassConfig[] sInstances;
		
		static {
			ClassConfig[] src = {
					new ClassConfig(false, false, false),
					new ClassConfig(false, false, true),
					new ClassConfig(false, true, false),
					new ClassConfig(false, true, true),
					new ClassConfig(true, false, false),
					new ClassConfig(true, false, true),
					new ClassConfig(true, true, false),
					new ClassConfig(true, true, true),
			};
			
			ClassConfig[] dst = new ClassConfig[src.length];
			
			for (ClassConfig config : src) {
				dst[config.getIndex()] = config;
			}
			sInstances = dst;
		}
		
		private final boolean markingSupported;
		private final boolean linkingSupported;
		private final boolean extendingSupported;
		private final int index;
		private final String classNameSuffix;

		public ClassConfig(boolean markingSupported, boolean linkingSupported, boolean extendingSupported) {
			this.markingSupported = markingSupported;
			this.linkingSupported = linkingSupported;
			this.extendingSupported = extendingSupported;
			StringBuilder sb = new StringBuilder();
			if (markingSupported) sb.append("_Markable");
			if (linkingSupported) sb.append("_Linkable");
			if (extendingSupported) sb.append("_Extensible");
			classNameSuffix = sb.toString();
			index = (markingSupported ? 1 : 0) + (linkingSupported ? 2 : 0) + (extendingSupported ? 4 : 0);
		}
		
		public boolean isLinkingSupported() {
			return linkingSupported;
		}
		
		public boolean isMarkingSupported() {
			return markingSupported;
		}

		public boolean isExtendingSupported() {
			return extendingSupported;
		}
		
		int getIndex() {
			return index;
		}
		
		String getClassNameSuffix() {
			return classNameSuffix;
		}
		
		// TODO object methods
		
		@Override
		public String toString() {
			return classNameSuffix;
		}
		
	}
	
	// fields
	
	private final RecordDef definition;
	private final String name;
	private final String source;
	private final Class<? extends LinearRecord>[] clss = new Class[ClassConfig.sInstances.length];
	private final Constructor<? extends LinearRecord>[] cons = new Constructor[ClassConfig.sInstances.length];
	private final HashSource<LinearRecord>[] sources = new HashSource[ClassConfig.sInstances.length];
	
	// constructors
	
	private DynamicRecordFactory(RecordDef definition, String name) {
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
			for (ClassConfig config : ClassConfig.sInstances) {
				clss[config.getIndex()] = (Class) compiler.getClassLoader().loadClass(packageName + "." + name + config.getClassNameSuffix());
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		try {
			for (int i = 0; i < cons.length; i++) {
				cons[i] = clss[i].getConstructor(consParams);
				sources[i] = (HashSource) clss[i].getField("HASH_SOURCE").get(null);
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RecordDef getDefinition() {
		return definition;
	}
	
	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public LinearRecord newRecord(ClassConfig config, LinearRecord record) {
		return newRecord(config, record, false);
	}

	public LinearRecord newRecord(ClassConfig config, LinearRecord record, boolean basis) {
		if (config == null) throw new IllegalArgumentException("null config");
		try {
			return cons[config.getIndex()].newInstance(record, basis);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public HashSource<LinearRecord> getHashSource(ClassConfig config) {
		if (config == null) throw new IllegalArgumentException("null config");
		return sources[config.getIndex()];
	}
	
	private String generateSource() {
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(packageName).append(";\n");
		for (ClassConfig config : ClassConfig.sInstances) {
			generateSource(sb, config);
		}
		return sb.toString();
	}

	private void generateSource(StringBuilder sb, ClassConfig config) {
		boolean linkable = config.isLinkingSupported();
		boolean markable = config.isMarkingSupported();
		boolean extensible = config.isExtendingSupported();
		String className = name + config.getClassNameSuffix(); 
		sb.append("\npublic class ").append(className).append(" implements ");
		if (linkable) {
			sb.append(LinkedRecord.class.getName());
		} else {
			sb.append(LinearRecord.class.getName());
		}
		sb.append(", Comparable");
		if (extensible) sb.append(", ").append(Extended.class.getName());
		sb.append(" {\n");

		// statics
		sb.append("\tprivate static final short limit = ").append(definition.getTypes().size()).append(";\n");
		sb.append("\tpublic static final ").append(HashSource.class.getName()).append(" HASH_SOURCE = new ").append(HashSource.class.getName()).append("() {\n");
		sb.append("\t\tpublic void sourceData(Object value, ").append(WriteStream.class.getName()).append(" out) {\n");
		sb.append("\t\t\tif (value != null) ((").append(className).append(") value).populateStream(out);\n");
		sb.append("\t\t}\n");
		sb.append("\t};\n");

		// fields
		if (definition.isOrdinal()) sb.append("\tprivate final long ordinal;\n");
		if (definition.isPositional()) sb.append("\tprivate final long position;\n");
		sb.append("\tprivate short field = 0;\n");
		if (markable) sb.append("\tprivate short mark = Short.MAX_VALUE;\n");
		{
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				sb.append("\tprivate final ").append(type).append(" f_").append(field++).append(";\n");
			}
		}
		if (linkable) {
			sb.append("\tprivate ").append(className).append(" next;\n");
			sb.append("\tprivate ").append(className).append(" prev;\n");
		}
		if (extensible) {
			sb.append("\tprivate Object extension;\n");
		}
		
		// constructors
		{
			sb.append("\tpublic ").append(className).append("(" + LinearRecord.class.getName() + " record, boolean basis) {\n");
			if (linkable) sb.append("\t\tnext = prev = this;\n");
			if (definition.isOrdinal()) sb.append("\t\tthis.ordinal = record.getOrdinal();\n");
			if (definition.isPositional()) sb.append("\t\tthis.position = record.getPosition();\n");
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
		sb.append("\tpublic long getOrdinal() {\n");
		sb.append("\t\treturn ").append(definition.isOrdinal() ? "ordinal" : "-1L").append(";\n");
		sb.append("\t}\n");

		sb.append("\tpublic long getPosition() {\n");
		sb.append("\t\treturn ").append(definition.isPositional() ? "position" : "-1L").append(";\n");
		sb.append("\t}\n");

		// next methods
		List<ColumnType> types = new ArrayList<ColumnType>();
		types.addAll(ColumnType.PRIMITIVE_TYPES);
		types.addAll(ColumnType.OBJECT_TYPES);
		for (ColumnType type : types) {
			sb.append("\tpublic ").append(type).append(" next").append(accessorName(type)).append("() {\n");
			sb.append("\t\tif (field == limit) throw new IllegalStateException(\"fields exhausted\");\n");
			sb.append("\t\tswitch(field++) {\n");
			int field = 0;
			for (ColumnType t : definition.getTypes()) {
				if (type == t) {
					sb.append("\t\t\tcase ").append(field).append(":\n");
					sb.append("\t\t\treturn f_").append(field).append(";\n");
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
			sb.append("\t\tthrow new UnsupportedOperationException(\"mark not supported\");\n");
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
		
		sb.append("\tpublic void release() { field = limit; }\n");

		// linked methods
		if (linkable) {
			sb.append("\tpublic void insertRecordBefore(").append(LinkedRecord.class.getName()).append(" record) {\n");
			sb.append("\t\tif (record == null) throw new IllegalArgumentException(\"null record\");\n");
			sb.append("\t\tif (!(record instanceof ").append(className).append(")) throw new IllegalArgumentException(\"incorrect record type\");\n");
			sb.append("\t\tif (next != this) throw new IllegalStateException(\"already linked\");\n");
			sb.append("\t\t").append(className).append(" that = (").append(className).append(") record;\n");
			sb.append("\t\tthis.next = that;\n");
			sb.append("\t\tthis.prev = that.prev;\n");
			sb.append("\t\tthat.prev.next = this;\n");
			sb.append("\t\tthat.prev = this;\n");
			sb.append("\t}\n");

			sb.append("\tpublic void insertRecordAfter(").append(LinkedRecord.class.getName()).append(" record) {\n");
			sb.append("\t\tif (record == null) throw new IllegalArgumentException(\"null record\");\n");
			sb.append("\t\tif (!(record instanceof ").append(className).append(")) throw new IllegalArgumentException(\"incorrect record type\");\n");
			sb.append("\t\tif (next != this) throw new IllegalStateException(\"already linked\");\n");
			sb.append("\t\t").append(className).append(" that = (").append(className).append(") record;\n");
			sb.append("\t\tthis.prev = that;\n");
			sb.append("\t\tthis.next = that.next;\n");
			sb.append("\t\tthat.next.prev = this;\n");
			sb.append("\t\tthat.next = this;\n");
			sb.append("\t}\n");

			sb.append("\tpublic ").append(LinkedRecord.class.getName()).append(" getNextRecord() {\n");
			sb.append("\t\treturn next;\n");
			sb.append("\t}\n");

			sb.append("\tpublic ").append(LinkedRecord.class.getName()).append(" getPreviousRecord() {\n");
			sb.append("\t\treturn prev;\n");
			sb.append("\t}\n");

			sb.append("\tpublic void removeRecord() {\n");
			sb.append("\t\tnext.prev = prev;\n");
			sb.append("\t\tprev.next = next;\n");
			sb.append("\t\tnext = this;\n");
			sb.append("\t\tprev = this;\n");
			sb.append("\t}\n");

			sb.append("\tpublic void replaceRecord(LinkedRecord record) {\n");
			sb.append("\t\tif (record == null) throw new IllegalArgumentException(\"null record\");\n");
			sb.append("\t\tif (!(record instanceof ").append(className).append(")) throw new IllegalArgumentException(\"incorrect record type\");\n");
			sb.append("\t\tif (next != this) throw new IllegalStateException(\"already linked\");\n");
			sb.append("\t\t").append(className).append(" that = (").append(className).append(") record;\n");
			sb.append("\t\tthis.next = that.next;\n");
			sb.append("\t\tthis.prev = that.prev;\n");
			sb.append("\t\tthis.next.prev = this;\n");
			sb.append("\t\tthis.prev.next = this;\n");
			sb.append("\t\tthat.next = that;\n");
			sb.append("\t\tthat.prev = that;\n");
			sb.append("\t}\n");

		}
		
		// extensible methods
		if (extensible) {
			sb.append("\tpublic void setExtension(Object extension) {\n");
			sb.append("\t\tthis.extension = extension;\n");
			sb.append("\t}\n");
			
			sb.append("\tpublic Object getExtension() {\n");
			sb.append("\t\treturn extension;");
			sb.append("\t}\n");
		}
		
		// comparable methods
		{
			sb.append("\tpublic int compareTo(Object obj) {\n");
			sb.append("\t\t" + className + " that = (" + className + ") obj;\n");
			//TODO support disordered sort
			for (ColumnDef column : definition.getOrderedColumns()) {
				int field = column.getIndex();
				ColumnOrder order = column.getOrder();
				boolean ascending = (order.getSort() != Sort.DESCENDING);
				ColumnType type = definition.getTypes().get(field);
				if (type == BOOLEAN_PRIMITIVE) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " ? -1 : 1;\n");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " " + (ascending ? '<' : '>') + " that.f_" + field + " ? -1 : 1;\n");
				} else {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") {\n");
					sb.append("\t\t\tif (this.f_" + field + " == null) return " + (order.isNullFirst() ? "-1" : "1") + ";\n");
					sb.append("\t\t\tif (that.f_" + field + " == null) return " + (order.isNullFirst() ? "1" : "-1") + ";\n");
					sb.append("\t\t\treturn ((Comparable)").append(ascending ? "this" : "that").append(".f_").append(field).append(").compareTo(((Comparable) ").append(ascending ? "that" : "this").append(".f_").append(field).append("));\n");
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
					sb.append("\t\th = (31 * h) ^ (f_").append(field).append(" ? 1231 : 1237);\n");
				} else if (type == LONG_PRIMITIVE) {
					sb.append("\t\th = (31 * h) ^ (int) (f_").append(field).append(" >> 32) ^ (int) f_" + field + ";\n");
				} else if (type == FLOAT_PRIMITIVE) {
					sb.append("\t\th = (31 * h) ^ Float.floatToIntBits(f_").append(field).append(");\n");
				} else if (type == DOUBLE_PRIMITIVE) {
					sb.append("\t\t{");
					{
						sb.append("\t\t\tlong tmp = Double.doubleToLongBits(f_").append(field).append(");\n");
						sb.append("\t\t\th = (31 * h) ^ (int) (tmp >> 32) ^ (int) tmp;\n");
					}
					sb.append("\t\t}");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\th = (31 * h) ^ f_").append(field).append(";\n");
				} else {
					sb.append("\t\tif (f_").append(field).append(" != null) h = (31 * h) ^ f_").append(field).append(".hashCode();\n");
				}
				field++;
			}
			sb.append("\t\treturn h;\n");
			sb.append("\t}\n");
		}

		{
			sb.append("\tpublic boolean equals(Object obj) {\n");
			sb.append("\t\tif (obj == this) return true;\n");
			sb.append("\t\tif (!(obj instanceof " + className +")) return false;\n");
			sb.append("\t\t" + className + " that = (" + className + ") obj;\n");
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

		// private methods
		{
			sb.append("\tvoid populateStream(").append(WriteStream.class.getName()).append(" out) {\n");
			int field = 0;
			for (ColumnType type : definition.getTypes()) {
				if (type.typeClass == CharSequence.class) {
					sb.append("\t\tif (f_").append(field).append(" != null) out.writeChars(f_").append(field).append(");\n");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\tout.write").append(accessorName(type)).append("(f_").append(field).append(");\n");
				} else {
					sb.append("\t\tif (f_").append(field).append(" != null) {\n");
					sb.append("\t\t\tout.write").append(accessorName(type)).append("(f_").append(field).append(".").append(accessorName(type).toLowerCase()).append("Value());\n");
					sb.append("\t\t}\n");
				}
				field++;
			}
			sb.append("\t}\n");
		}
		
		sb.append("}\n");
	}

	private void generateRecordCopy(StringBuilder sb, List<ColumnDef> columns) {
		for (ColumnDef column : columns) {
			if (column == null) {
				sb.append("\t\t\trecord.skipNext();\n");
			} else {
				int field = column.getIndex();
				ColumnType type = column.getType();
				String accessorName = accessorName(type);
				if (type.typeClass.isPrimitive()) {
					sb.append("\t\t\tf_").append(field).append(" = record.next").append(accessorName).append("();\n");
				} else if (type == ColumnType.STRING_OBJECT) {
					sb.append("\t\t\t").append(type).append(" tmp_").append(field).append(" = record.next").append(accessorName).append("();\n");
					sb.append("\t\t\tif (tmp_").append(field).append(" instanceof ").append(ProcessScoped.class.getName()).append(") tmp_").append(field).append(" = ").append(" tmp_").append(field).append(".toString();");
					sb.append("\t\t\tf_").append(field).append(" = record.wasNull() ? null : tmp_").append(field).append(";\n");
				} else {
					sb.append("\t\t\t").append(type).append(" tmp_").append(field).append(" = record.next").append(accessorName).append("();\n");
					sb.append("\t\t\tf_").append(field).append(" = record.wasNull() ? null : tmp_").append(field).append(";\n");
				}
			}
		}
	}

}
