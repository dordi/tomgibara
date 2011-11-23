package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.def.ColumnType;

public class RecordCompactor {


	private final ColumnType[] types;
	private final ColumnCompactor[] compactors;
	
	public RecordCompactor(ProcessContext context) {
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalArgumentException("context has no column types");
		RecordStats stats = context.getRecordStats();
		if (stats == null) throw new IllegalArgumentException("context has no record stats");
		
		
		ColumnCompactor[] compactors = new ColumnCompactor[types.size()];
		List<ColumnStats> list = stats.getColumnStats();
		for (int i = 0; i < compactors.length; i++) {
			compactors[i] = new ColumnCompactor(list.get(i));
		}
		
		this.types = (ColumnType[]) types.toArray(new ColumnType[types.size()]);
		this.compactors = compactors;
	}

	public int compact(CodedWriter writer, LinearRecord record) {
		int c = 0;
		for (int i = 0; i < compactors.length; i++) {
			ColumnCompactor compactor = compactors[i];
			switch (types[i]) {
			case BOOLEAN_PRIMITIVE:
			case BOOLEAN_WRAPPER:
			{
				boolean value = record.nextBoolean();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeBoolean(writer, value);
				break;
			}
			case BYTE_PRIMITIVE:
			case BYTE_WRAPPER:
			{
				byte value = record.nextByte();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeInt(writer, value);
				break;
			}
			case SHORT_PRIMITIVE:
			case SHORT_WRAPPER:
			{
				short value = record.nextShort();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeInt(writer, value);
				break;
			}
			case INT_PRIMITIVE:
			case INT_WRAPPER:
			{
				int value = record.nextInt();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeInt(writer, value);
				break;
			}
			case LONG_PRIMITIVE:
			case LONG_WRAPPER:
			{
				long value = record.nextLong();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeLong(writer, value);
				break;
			}
			case FLOAT_PRIMITIVE:
			case FLOAT_WRAPPER:
			{
				float value = record.nextFloat();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeFloat(writer, value);
				break;
			}
			case DOUBLE_PRIMITIVE:
			case DOUBLE_WRAPPER:
			{
				double value = record.nextDouble();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeDouble(writer, value);
				break;
			}
			case CHAR_PRIMITIVE:
			case CHAR_WRAPPER:
			{
				char value = record.nextChar();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeChar(writer, value);
				break;
			}
			case STRING_OBJECT:
			{
				String value = record.nextString();
				boolean isNull = record.wasNull();
				c += compactor.encodeNull(writer, isNull);
				if (!isNull) c += compactor.encodeString(writer, value);
				break;
			}
				default: throw new IllegalStateException("Unsupported type");
			}
		}
		return c;
	}
	
}
