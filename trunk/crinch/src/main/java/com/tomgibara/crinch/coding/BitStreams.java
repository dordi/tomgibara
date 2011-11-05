package com.tomgibara.crinch.coding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.tomgibara.crinch.bits.BitWriter;

public class BitStreams {

	//TODO test
	public static int writePrimitiveArray(CodedWriter writer, Object array) {
		if (array == null) throw new IllegalArgumentException("null array");
		Class<?> clss = array.getClass();
		if (!clss.isArray()) throw new IllegalArgumentException("not an array");
		Class<?> comp = clss.getComponentType();
		if (!comp.isPrimitive()) throw new IllegalArgumentException("array components not primitives");
		
		int length = Array.getLength(array);
		writer.writePositiveInt(length + 1);
		//TODO would love a switch statement here
		int c = 0;
		if (comp == boolean.class) {
			BitWriter w = writer.getWriter();
			boolean[] a = (boolean[]) array;
			for (int i = 0; i < a.length; i++) c += w.writeBoolean(a[i]);
		} else if (comp == byte.class) {
			byte[] a = (byte[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeSignedInt(a[i]);
		} else if (comp == short.class) {
			short[] a = (short[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeSignedInt(a[i]);
		} else if (comp == int.class) {
			int[] a = (int[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeSignedInt(a[i]);
		} else if (comp == long.class) {
			long[] a = (long[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeSignedLong(a[i]);
		} else if (comp == float.class) {
			//TODO add float
			float[] a = (float[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeDouble(a[i]);
		} else if (comp == double.class) {
			double[] a = (double[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeDouble(a[i]);
		} else if (comp == char.class) {
			char[] a = (char[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writePositiveInt(a[i] + 1);
		} else {
			throw new UnsupportedOperationException("unsupported primitive type " + comp.getName());
		}
		return c;
	}

	//TODO add other methods
	public static int[] readIntArray(CodedReader reader) {
		int length = reader.readPositiveInt() - 1;
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = reader.readSignedInt();
		}
		return a;
	}

	public static <E extends Enum<?>> int writeEnumArray(CodedWriter writer, E[] enums) {
		int length = enums.length;
		writer.writePositiveInt(length + 1);
		int c = 0;
		for (int i = 0; i < length; i++) {
			c += writer.writePositiveInt(enums[i].ordinal() + 1);
		}
		return c;
	}
	
	public static <E extends Enum<?>> E[] readEnumArray(CodedReader reader, Class<E> enumClass) {
		if (enumClass == null) throw new IllegalArgumentException("null enumClass");
		E[] values = enumClass.getEnumConstants();
		if (values == null) throw new IllegalArgumentException("not an enum class");
		int length = reader.readPositiveInt() - 1;
		E[] a = (E[]) Array.newInstance(enumClass, length);
		for (int i = 0; i < length; i++) {
			a[i] = values[reader.readSignedInt() - 1];
		}
		return a;
	}

	public static <E extends Enum<?>> int writeEnumList(CodedWriter writer, List<E> list) {
		int length = list.size();
		writer.writePositiveInt(length + 1);
		int c = 0;
		for (E e : list) c += writer.writePositiveInt(e.ordinal() + 1);
		return c;
	}
	
	public static <E extends Enum<?>> List<E> readEnumList(CodedReader reader, Class<E> enumClass) {
		if (enumClass == null) throw new IllegalArgumentException("null enumClass");
		E[] values = enumClass.getEnumConstants();
		if (values == null) throw new IllegalArgumentException("not an enum class");
		int length = reader.readPositiveInt() - 1;
		List<E> list = new ArrayList<E>(length);
		for (int i = 0; i < length; i++) list.add( values[reader.readPositiveInt() - 1] );
		return list;
	}

}
