package com.tomgibara.crinch.record.index;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.tomgibara.crinch.hashing.Hash;
import com.tomgibara.crinch.hashing.HashRange;
import com.tomgibara.crinch.hashing.HashSource;
import com.tomgibara.crinch.hashing.Hashes;
import com.tomgibara.crinch.hashing.Murmur3_32Hash;
import com.tomgibara.crinch.hashing.PRNGMultiHash;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.compact.RecordCompactor;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.util.WriteStream;

//TODO unique keys will need to store extra state to link truly matching keys
public class HashConsumer implements RecordConsumer<LinearRecord> {

	// constructor state
	private final SubRecordDef subRecDef;
	// prepared state
	private ProcessContext context;
	private RecordStats recStats;
	private BigInteger recordCount;
	private RecordDef recordDef;
	private RecordCompactor compactor;
	private boolean uniqueKeys;
	private ClassConfig config;
	DynamicRecordFactory factory;
	private Hash<LinearRecord> hash;
	//private PositionStats posStats;
	private File file;
	// pass state
	private long[] positions;
	private int[] sizes;
	
	public HashConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		//posStats = new PositionStats(context);
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalStateException("no stats");
		recordCount = BigInteger.valueOf(recStats.getRecordCount());
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalStateException("no record definition");
		def = def.asBasis();
		recordDef = subRecDef == null ? def : def.asSubRecord(subRecDef);
		if (recordDef.getColumns().isEmpty()) throw new IllegalStateException("record definition has no columns");
		hash = new Murmur3_32Hash<LinearRecord>(new RecordHashSource(recordDef.getTypes()));
		//TODO should base on size of hash
		if (recStats.getRecordCount() > Integer.MAX_VALUE) throw new UnsupportedOperationException("long record counts not currently supported.");
		uniqueKeys = compactor.getColumnStats(0).isUnique();
		config = new ClassConfig(true, false, false);
		factory = DynamicRecordFactory.getInstance(recordDef);
		//TODO should be configurable
		//hash = new PRNGMultiHash<LinearRecord>("SHA1PRNG", chooseHashSource(recordDef.getTypes().get(0)), HashRange.POSITIVE_INT_RANGE);
		file = context.file("positions", false, recordDef);
		if (context.isClean()) file.delete();
	}

	@Override
	public int getRequiredPasses() {
		if (positions == null) return 2;
		if (!file.isFile()) return 1;
		return 0;
	}

	@Override
	public void beginPass() {
		context.setPassName("Building hash table");
		if (positions == null) {
			positions = new long[ recordCount.intValue() ];
			Arrays.fill(positions, -1L);
		} else {
			throw new UnsupportedOperationException("File creation not supported yet");
		}
	}

	@Override
	public void consume(LinearRecord record) {
		long ordinal = record.getOrdinal();
		if (ordinal < 0L) throw new IllegalArgumentException("record without ordinal");
		LinearRecord subRec = factory.newRecord(config, record, subRecDef != null);
		subRec.mark();
		/*
		BigInteger bigInt = hash.hashAsBigInt(subRec);
		int index = bigInt.mod(recordCount).intValue();
		*/
		int index = (hash.hashAsInt(subRec) & 0x7fffffff) % (int) recStats.getRecordCount();
		positions[index] = ordinal;
//		System.out.println(index + " -> " + ordinal);
		
	}
	
	@Override
	public void endPass() {
		int count = 0;
//		Arrays.sort(positions);
//		for (int i = 1; i < positions.length; i++) {
//			if (positions[i] == positions[i-1]) count++;
//		}
		for (int i = 0; i < positions.length; i++) {
		if (positions[i] < 0) {
			count++;
		}
	}
		System.out.println("CLASHES: " + count);
		System.out.println("RECORDS: " + recordCount);
	}
	
	@Override
	public void complete() {
		cleanup();
	}
	
	@Override
	public void quit() {
		cleanup();
	}
	
	// wipe references to free memory
	private void cleanup() {
	}

	// inner classes
	
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
	
	private static class RecordHashSource implements HashSource<LinearRecord> {
		
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
	
}
