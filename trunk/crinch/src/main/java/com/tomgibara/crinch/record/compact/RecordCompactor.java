package com.tomgibara.crinch.record.compact;

import java.util.List;

import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.record.ColumnStats;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.ValueParser;

class RecordCompactor {


	private final ValueParser parser;
	private final ColumnType[] types;
	private final RecordStats stats;
	private final ColumnCompactor[] compactors;
	
	RecordCompactor(ValueParser parser, ColumnType[] types, RecordStats stats) {
		this.parser = parser;
		this.types = types;
		this.stats = stats;
		ColumnCompactor[] compactors = new ColumnCompactor[types.length];
		List<ColumnStats> list = stats.getColumnStats();
		for (int i = 0; i < compactors.length; i++) {
			compactors[i] = new ColumnCompactor(list.get(i));
		}
		this.compactors = compactors;
	}

	int compact(CodedWriter writer, LinearRecord record) {
		int c = 0;
		for (int i = 0; i < compactors.length; i++) {
			ColumnCompactor compactor = compactors[i];
			String str = record.nextString();
			boolean isNull = str == null;
			c += compactor.encodeNull(writer, isNull);
			if (isNull) continue;
			switch (types[i]) {
			case BOOLEAN_PRIMITIVE:
			case BOOLEAN_WRAPPER:
				c += compactor.encodeBoolean(writer, parser.parseBoolean(str));
				break;
			case BYTE_PRIMITIVE:
			case BYTE_WRAPPER:
				c += compactor.encodeInt(writer, parser.parseByte(str));
				break;
			case SHORT_PRIMITIVE:
			case SHORT_WRAPPER:
				c += compactor.encodeInt(writer, parser.parseShort(str));
				break;
			case INT_PRIMITIVE:
			case INT_WRAPPER:
				c += compactor.encodeInt(writer, parser.parseInt(str));
				break;
			case LONG_PRIMITIVE:
			case LONG_WRAPPER:
				c += compactor.encodeLong(writer, parser.parseLong(str));
				break;
			case FLOAT_PRIMITIVE:
			case FLOAT_WRAPPER:
				c += compactor.encodeFloat(writer, parser.parseFloat(str));
				break;
			case DOUBLE_PRIMITIVE:
			case DOUBLE_WRAPPER:
				c += compactor.encodeDouble(writer, parser.parseDouble(str));
				break;
			case CHAR_PRIMITIVE:
			case CHAR_WRAPPER:
				c += compactor.encodeInt(writer, parser.parseChar(str));
				break;
			case STRING_OBJECT:
				c += compactor.encodeString(writer, parser.parseString(str));
				break;
				default: throw new IllegalStateException("Unsupported type");
			}
		}
		return c;
	}
	
	RecordDecompactor decompactor() {
		return new RecordDecompactor(stats);
	}
	
}
