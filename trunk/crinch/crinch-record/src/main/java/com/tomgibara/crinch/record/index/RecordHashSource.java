package com.tomgibara.crinch.record.index;

import java.util.List;

import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.util.WriteStream;

class RecordHashSource implements HashSource<LinearRecord> {
	
	private static class BooleanHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			boolean value = record.nextBoolean();
			if (!record.wasNull()) out.writeBoolean(value);
		}
		
	}
	
	private static class ByteHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			byte value = record.nextByte();
			if (!record.wasNull()) out.writeByte(value);
		}
		
	}
	
	private static class CharHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			char value = record.nextChar();
			if (!record.wasNull()) out.writeChar(value);
		}
		
	}
	
	private static class DoubleHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			double value = record.nextDouble();
			if (!record.wasNull()) out.writeDouble(value);
		}
		
	}
	
	private static class FloatHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			float value = record.nextFloat();
			if (!record.wasNull()) out.writeFloat(value);
		}
		
	}
	
	private static class IntHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			int value = record.nextInt();
			if (!record.wasNull()) out.writeInt(value);
		}
		
	}
	
	private static class LongHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			long value = record.nextLong();
			if (!record.wasNull()) out.writeLong(value);
		}
		
	}
	
	private static class ShortHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			short value = record.nextShort();
			if (!record.wasNull()) out.writeShort(value);
		}
		
	}
	
	private static class StringHashSource implements HashSource<LinearRecord> {
		
		@Override
		public void sourceData(LinearRecord record, WriteStream out) {
			CharSequence value = record.nextString();
			if (!record.wasNull()) out.writeChars(value.toString());
		}
		
	}

	private static HashSource<LinearRecord> chooseHashSource(ColumnType type) {
		switch (type) {
		case BOOLEAN_PRIMITIVE:
		case BOOLEAN_WRAPPER:
			return new BooleanHashSource();
		case BYTE_PRIMITIVE:
		case BYTE_WRAPPER:
			return new ByteHashSource();
		case CHAR_PRIMITIVE:
		case CHAR_WRAPPER:
			return new CharHashSource();
		case DOUBLE_PRIMITIVE:
		case DOUBLE_WRAPPER:
			return new DoubleHashSource();
		case FLOAT_PRIMITIVE:
		case FLOAT_WRAPPER:
			return new FloatHashSource();
		case INT_PRIMITIVE:
		case INT_WRAPPER:
			return new IntHashSource();
		case LONG_PRIMITIVE:
		case LONG_WRAPPER:
			return new LongHashSource();
		case SHORT_PRIMITIVE:
		case SHORT_WRAPPER:
			return new ShortHashSource();
		case STRING_OBJECT:
			return new StringHashSource();
		default:
			throw new IllegalStateException("Unsupported type: " + type);
		}
	}

	private final HashSource<LinearRecord>[] hashSources;
	
	RecordHashSource(List<ColumnType> types) {
		hashSources = new HashSource[types.size()];
		for (int i = 0; i < hashSources.length; i++) {
			hashSources[i] = chooseHashSource(types.get(i));
		}
	}
	
	@Override
	public void sourceData(LinearRecord value, WriteStream out) {
		for (int i = 0; i < hashSources.length; i++) {
			hashSources[i].sourceData(value, out);
		}
	}
	
}