package com.tomgibara.crinch.record.index;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
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
import com.tomgibara.crinch.record.process.ProcessLogger.Level;
import com.tomgibara.crinch.record.util.UniquenessChecker;
import com.tomgibara.crinch.util.WriteStream;

//TODO should avoid confirming uniqueness in single column cases
//TODO should allow confirming uniqueness to be overridden
public class HashConsumer implements RecordConsumer<LinearRecord> {

	private static final int HASH_COUNT = 3;
	private static final int ENTRY_SIZE = HASH_COUNT + 2;
	private static final float DEFAULT_LOAD_FACTOR = 0.85f;
	//TODO estimate this properly!
	private static final double AVERAGE_KEY_SIZE_IN_BYTES = 250.0;
	
	// constructor state
	private final SubRecordDef subRecDef;
	@SuppressWarnings("unchecked")
	private final Murmur3_32Hash<LinearRecord>[] hashes = new Murmur3_32Hash[HASH_COUNT];
	private final Random random = new Random();
	private final int[] first = new int[HASH_COUNT + 2];
	private final int[] second = new int[HASH_COUNT + 2];
	// prepared state
	private ProcessContext context;
	private RecordStats recStats;
	private BigInteger recordCount;
	private RecordDef recordDef;
	private RecordCompactor compactor;
	private ClassConfig config;
	DynamicRecordFactory factory;
	private RecordHashSource hashSource;
	private HashStats hashStats;
	private File file;
	// pass state
	private UniquenessChecker<LinearRecord> checker;
	float loadFactor;
	private int[] map;
	private boolean passAborted;
	private long largestValue = -1L;
	
	public HashConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalStateException("no stats");
		hashStats = new HashStats(context);
		recordCount = BigInteger.valueOf(recStats.getRecordCount());
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		RecordDef def = context.getRecordDef();
		if (def == null) throw new IllegalStateException("no record definition");
		def = def.asBasis();
		recordDef = subRecDef == null ? def : def.asSubRecord(subRecDef);
		if (recordDef.getColumns().isEmpty()) throw new IllegalStateException("record definition has no columns");
		hashSource = new RecordHashSource(recordDef.getTypes());
		config = new ClassConfig(true, false, false);
		factory = DynamicRecordFactory.getInstance(recordDef);
		//TODO pull from record def
		loadFactor = DEFAULT_LOAD_FACTOR;
		file = context.file("hash", false, recordDef);
		if (context.isClean()) file.delete();
		Boolean skipUniqueCheck = recordDef.getBooleanProperty("hash.skipUniqueCheck");
		checker = skipUniqueCheck != null && skipUniqueCheck ? null : new UniquenessChecker<LinearRecord>(recordCount.longValue(), AVERAGE_KEY_SIZE_IN_BYTES, hashSource);
	}

	@Override
	public int getRequiredPasses() {
		if (checker != null) {
			return 2;
		} else if (!file.isFile()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public void beginPass() {
		if (checker != null) {
			context.setPassName("Confirming uniqueness of hash keys");
			checker.beginPass();
		} else {
			context.setPassName("Building hash table");
			// set up stats
			// table size
			double tableSize = recordCount.doubleValue() / loadFactor;
			if (tableSize > Integer.MAX_VALUE / ENTRY_SIZE) throw new IllegalStateException("large record counts not currently supported");
			hashStats.tableSize = (int) Math.ceil(tableSize);
			// hash seeds
			int[] hashSeeds = new int[HASH_COUNT];
			for (int i = 0; i < HASH_COUNT; i++) {
				hashSeeds[i] = random.nextInt();
				hashes[i] = new Murmur3_32Hash<LinearRecord>(hashSource, hashSeeds[i]);
			}
			hashStats.hashSeeds = hashSeeds;
			// init map
			if (map == null || map.length != hashStats.tableSize * ENTRY_SIZE) {
				map = null;
				map = new int[hashStats.tableSize * ENTRY_SIZE];
			}
			Arrays.fill(map, -1);
		}
		passAborted = false;
	}

	@Override
	public void consume(LinearRecord record) {
		if (passAborted) return;
		LinearRecord subRec = factory.newRecord(config, record, subRecDef != null);
		subRec.mark();
		if (checker != null) {
			passAborted = checker.add(subRec);
		} else {
			long value;
			//TODO use position by default
			if (true) {
				value = record.getOrdinal();
				if (value < 0L) throw new IllegalArgumentException("record without ordinal");
			}
			if (largestValue < value) largestValue = value;
			
			first[0] = (int) (value >> 32);
			first[1] = (int) value;
			for (int i = 0; i < HASH_COUNT; i++) {
				subRec.reset();
				first[2 + i] = ENTRY_SIZE * ((hashes[i].hashAsInt(subRec) & 0x7fffffff) % (int) hashStats.tableSize);
			}
			putEntry(first, 0);
		}
		
	}
	
	private void putEntry(int[] entry, int attempts) {
		//TODO need to set attempt limit correctly
		if (attempts >= 1000) {
			context.getLogger().log(Level.WARN, "Hash building failed, may retry");
			passAborted = true;
		} else {
			for (int i = 0; i < HASH_COUNT; i++) {
				int index = entry[2 + i];
				if (map[index] == -1 && map[index+1] == -1) {
					System.arraycopy(entry, 0, map, index, ENTRY_SIZE);
					return;
				}
			}
			int[] other = entry == first ? second : first;
			int i = random.nextInt(HASH_COUNT);
			int index = entry[2 + i];
			System.arraycopy(map, index, other, 0, ENTRY_SIZE);
			System.arraycopy(entry, 0, map, index, ENTRY_SIZE);
			putEntry(other, attempts + 1);
		}
	}
	
	@Override
	public void endPass() {
		if (checker != null) {
			checker.endPass();
			if (checker.isUniquenessDetermined()) {
				if (checker.isUnique()) {
					// we're finished with the checker, prevent further checking
					checker = null;
				} else {
					throw new IllegalStateException("hash keys not unique");
				}
			}
		} else {
			if (!passAborted) {
				// record final stats info
				hashStats.valueBits = 64 - Long.numberOfLeadingZeros(largestValue + 1);
				// write the stats
				hashStats.write();
				// write the table
				writeFile();
				// clear memory asap
				map = null;
			}
		}
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

	private void writeFile() {
		CodedStreams.writeToFile(new CodedStreams.WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				final BitWriter w = writer.getWriter();
				final int[] map = HashConsumer.this.map;
				final int valueBits = hashStats.valueBits;
				for (int i = 0; i < map.length; i += ENTRY_SIZE) {
					long value = (((long) map[i]) << 32) | (map[i+1] & 0x00000000ffffffffL);
					w.write(value, valueBits);
				}
			}
		}, hashStats.coding, file);
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
