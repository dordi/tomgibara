package com.tomgibara.crinch.record.dynamic;

import static com.tomgibara.crinch.record.compact.ColumnType.BOOLEAN_PRIMITIVE;
import static com.tomgibara.crinch.record.compact.ColumnType.LONG_PRIMITIVE;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.codehaus.janino.SimpleCompiler;

import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.record.AbstractRecord;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.compact.ColumnType;
import com.tomgibara.crinch.util.WriteStream;

public class DynamicRecordFactory {

	public static class Order {
		
		final int index;
		final boolean ascending;
		final boolean nullFirst;
		
		public Order(int index, boolean ascending, boolean nullFirst) {
			this.index = index;
			this.ascending = ascending;
			this.nullFirst = nullFirst;
		}
		
	}
	
	public static class Definition {
		
		private final List<ColumnType> types;
		private final List<Order> orders;
	
		public Definition(List<ColumnType> types, Order... orders) {
			//TODO need to verify input
			this.types = types;
			this.orders = Arrays.asList(orders);
		}
		
	}
	
	private static HashSource<Definition> hashSource = new HashSource<Definition>() {
		
		@Override
		public void sourceData(Definition definition, WriteStream out) {
			for (ColumnType type : definition.types) {
				out.writeInt(type.ordinal());
			}
			for (Order order: definition.orders) {
				out.writeInt(order.index);
				out.writeBoolean(order.ascending);
			}
		}
	};
	
	private static final int hashDigits = 10;
	
	private static HashRange hashRange = new HashRange(BigInteger.ZERO, BigInteger.ONE.shiftLeft(4 * hashDigits));
	
	private static PRNGMultiHash<Definition> hash = new PRNGMultiHash<Definition>(hashSource, hashRange);
	
	private static final String namePattern = "DynRec_%0" + hashDigits + "X";
	
	private static String packageName = "com.tomgibara.crinch.record.dynamic";
	
	private static String name(Definition definition) {
		return String.format(namePattern, hash.hashAsBigInt(definition));
	}
	
	private static final Map<String, DynamicRecordFactory> factories = new HashMap<String, DynamicRecordFactory>();
	
	private static final Class<?>[] consParams = { LinearRecord.class };
	
	public static DynamicRecordFactory getInstance(Definition definition) {
		String name = name(definition);
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
	
	private final Definition definition;
	private final String name;
	private final String source;
	private final Class<? extends LinearRecord> clss;
	private final Constructor<? extends LinearRecord> cons;
	
	// constructors
	
	private DynamicRecordFactory(Definition definition, String name) {
		this.definition = definition;
		this.name = name;
		source = generateSource();
		System.out.println(source);
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

	public Definition getDefinition() {
		return definition;
	}
	
	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}
	
	public LinearRecord newRecord(LinearRecord record) {
		try {
			return cons.newInstance(record);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String generateSource() {
		//TODO move to ColumnType
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(packageName).append(";\n");
		sb.append("public class " + name).append(" extends " + AbstractRecord.class.getName() + " implements " + LinearRecord.class.getName() + ", Comparable {\n");
		sb.append("\tprivate static final int limit = ").append(definition.types.size()).append(";\n");
		
		// fields
		sb.append("\tprivate int field = 0;\n");
		{
			int field = 0;
			for (ColumnType type : definition.types) {
				sb.append("\tfinal ").append(type).append(" f_").append(field++).append(";\n");
			}
		}
		
		// constructors
		{
			sb.append("\tpublic ").append(name).append("() {\n");
			sb.append("\t}\n");
		}
		
		{
			sb.append("\tpublic ").append(name).append("(" + LinearRecord.class.getName() + " record) {\n");
			sb.append("\t\tsuper(record);\n");
			int field = 0;
			for (ColumnType type : definition.types) {
				sb.append("\t\tf_").append(field).append(" = record.next").append(Character.toUpperCase(type.toString().charAt(0))).append(type.toString().substring(1)).append("();\n");
				field++;
			}
			sb.append("\t}\n");
		}
		
		// next methods
		List<ColumnType> types = new ArrayList<ColumnType>();
		types.addAll(ColumnType.PRIMITIVE_TYPES);
		types.addAll(ColumnType.OBJECT_TYPES);
		for (ColumnType type : types) {
			sb.append("\tpublic ").append(type).append(" next").append(Character.toUpperCase(type.toString().charAt(0))).append(type.toString().substring(1)).append("() {\n");
			sb.append("\t\tif (field == limit) throw new IllegalStateException(\"fields exhausted\");\n");
			sb.append("\t\tswitch(field) {\n");
			int field = 0;
			for (ColumnType t : definition.types) {
				if (type == t) {
					sb.append("\t\t\tcase ").append(field).append(": return f_").append(field).append(";\n");
				}
				field++;
			}
			sb.append("\t\tdefault: throw new IllegalStateException(\"Field \" + field + \" not of type ").append(type).append("\");\n");
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
			for (ColumnType type : definition.types) {
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
		
		sb.append("\tpublic void exhaust() { field = limit; }\n");

		// comparable methods
		{
			sb.append("\tpublic int compareTo(Object obj) {\n");
			sb.append("\t\t" + name + " that = (" + name + ") obj;\n");
			for (Order order : definition.orders) {
				int field = order.index;
				ColumnType type = definition.types.get(field);
				if (type == BOOLEAN_PRIMITIVE) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " ? -1 : 1;\n");
				} else if (type.typeClass.isPrimitive()) {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") return this.f_" + field + " " + (order.ascending ? '<' : '>') + " that.f_" + field + " ? -1 : 1;\n");
				} else {
					sb.append("\t\tif (this.f_" + field + " != that.f_" + field + ") {\n");
					sb.append("\t\t\tif (this.f_" + field + " == null) return " + (order.nullFirst ? "-1" : "1") + ";\n");
					sb.append("\t\t\tif (that.f_" + field + " == null) return " + (order.nullFirst ? "1" : "-1") + ";\n");
					sb.append("\t\t\treturn " + (order.ascending ? "this" : "that") + ".f_" + field + ".compareTo(" + (order.ascending ? "that" : "this") + ".f_" + field + ");\n");
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
			for (ColumnType type : definition.types) {
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
			for (ColumnType type : definition.types) {
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
			for (ColumnType type : definition.types) {
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
	
}
