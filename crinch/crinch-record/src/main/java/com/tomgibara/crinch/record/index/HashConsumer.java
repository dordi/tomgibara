package com.tomgibara.crinch.record.index;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.hashing.Murmur3_32Hash;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.def.RecordDef;
import com.tomgibara.crinch.record.def.SubRecordDef;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory;
import com.tomgibara.crinch.record.dynamic.DynamicRecordFactory.ClassConfig;
import com.tomgibara.crinch.record.process.ProcessContext;
import com.tomgibara.crinch.record.process.ProcessLogger.Level;
import com.tomgibara.crinch.record.util.UniquenessChecker;

//TODO should avoid confirming uniqueness in single column cases
//TODO should allow confirming uniqueness to be overridden
//TODO should allow hash to store both position and ordinal
public class HashConsumer implements RecordConsumer<LinearRecord> {

	private static final int HASH_COUNT = 3;
	private static final float DEFAULT_LOAD_FACTOR = 0.90f;
	//TODO estimate this properly!
	private static final double AVERAGE_KEY_SIZE_IN_BYTES = 250.0;

	private static ClassConfig sConfig = new ClassConfig(true, false, false);
	
	// constructor state
	private final SubRecordDef subRecDef;
	@SuppressWarnings("unchecked")
	private final Murmur3_32Hash<LinearRecord>[] hashes = new Murmur3_32Hash[HASH_COUNT];
	private final Random random = new Random();
	// prepared state
	private ProcessContext context;
	private RecordStats recStats;
	private BigInteger recordCount;
	DynamicRecordFactory factory;
	private RecordHashSource hashSource;
	private HashStats hashStats;
	private int recordSize;
	private int entrySize;
	private int[] first;
	private int[] second;
	private int maxAttempts;

	private File file;
	// pass state
	private UniquenessChecker<LinearRecord> checker;
	float loadFactor;
	private int[] map;
	private boolean passAborted;
	private long largestOrdinal = -1L;
	private long largestPosition = -1L;
	
	// not much point passing null in here, but we support it
	public HashConsumer(SubRecordDef subRecDef) {
		this.subRecDef = subRecDef;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
		recStats = context.getRecordStats();
		if (recStats == null) throw new IllegalStateException("no stats");
		hashStats = new HashStats(context, subRecDef);
		RecordDef recordDef = hashStats.definition;
		recordCount = BigInteger.valueOf(recStats.getRecordCount());
		List<ColumnType> types = context.getColumnTypes();
		if (types == null) throw new IllegalStateException("no types");
		if (recordDef.getColumns().isEmpty()) throw new IllegalStateException("record definition has no columns");
		hashSource = new RecordHashSource(recordDef.getTypes());
		factory = DynamicRecordFactory.getInstance(recordDef);
		//TODO pull from record def
		loadFactor = DEFAULT_LOAD_FACTOR;
		file = context.file("hash", false, recordDef);
		if (context.isClean()) file.delete();
		Boolean skipUniqueCheck = recordDef.getBooleanProperty("hash.skipUniqueCheck");
		checker = skipUniqueCheck != null && skipUniqueCheck ? null : new UniquenessChecker<LinearRecord>(recordCount.longValue(), AVERAGE_KEY_SIZE_IN_BYTES, hashSource);
		Boolean ordinal = recordDef.getBooleanProperty("hash.ordinal");
		Boolean positional = recordDef.getBooleanProperty("hash.positional");
		hashStats.ordinal = ordinal == null ? true : ordinal.booleanValue();
		hashStats.positional = positional == null ? true : positional.booleanValue();
		if (!hashStats.ordinal && !hashStats.positional) throw new IllegalArgumentException("definition specifies neither positional nor ordinal");
		recordSize = hashStats.ordinal && hashStats.positional ? 4 : 2;
		entrySize = recordSize + hashes.length;
		first = new int[entrySize];
		second = new int[entrySize];
		//TODO how should this depend on the number of hash functions?
		maxAttempts = (int) (Math.log(recordCount.doubleValue()) / Math.log1p(0.001));
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
			if (tableSize > Integer.MAX_VALUE / entrySize) throw new IllegalStateException("large record counts not currently supported");
			hashStats.tableSize = (int) Math.ceil(tableSize);
			// hash seeds
			int[] hashSeeds = new int[HASH_COUNT];
			for (int i = 0; i < HASH_COUNT; i++) {
				hashSeeds[i] = random.nextInt();
				hashes[i] = new Murmur3_32Hash<LinearRecord>(hashSource, hashSeeds[i]);
			}
			hashStats.hashSeeds = hashSeeds;
			// init map
			if (map == null || map.length != hashStats.tableSize * entrySize) {
				map = null;
				map = new int[hashStats.tableSize * entrySize];
			}
			Arrays.fill(map, -1);
		}
		passAborted = false;
	}

	@Override
	public void consume(LinearRecord record) {
		if (passAborted) return;
		LinearRecord subRec = factory.newRecord(sConfig, record, subRecDef != null);
		subRec.mark();
		if (checker != null) {
			passAborted = checker.add(subRec);
		} else {
			// get position
			long position;
			if (hashStats.positional) {
				position = record.getPosition();
				if (position < 0) throw new IllegalArgumentException("record without position");
				if (largestPosition < position) largestPosition = position;
			} else {
				position = -1L;
			}
			
			// get ordinal
			long ordinal;
			if (hashStats.ordinal) {
				ordinal = record.getOrdinal();
				if (ordinal < 0) throw new IllegalArgumentException("record without ordinal");
				if (largestOrdinal < ordinal) largestOrdinal = ordinal;
			} else {
				ordinal = -1L;
			}
			
			// create entry
			int i = 0;
			if (hashStats.positional) {
				first[i++] = (int) (position >> 32);
				first[i++] = (int) position;
			}
			if (hashStats.ordinal) {
				first[i++] = (int) (ordinal >> 32);
				first[i++] = (int) ordinal;
			}
			for (int j = 0; j < HASH_COUNT; j++) {
				subRec.reset();
				//TODO use hash reranging
				first[i++] = entrySize * ((hashes[j].hashAsInt(subRec) & 0x7fffffff) % hashStats.tableSize);
			}
			
			//put entry
			putEntry(first, 0);
		}
		
	}
	
	private void putEntry(int[] entry, int attempts) {
		if (attempts >= maxAttempts) {
			context.getLogger().log(Level.WARN, "Hash building failed, may retry");
			passAborted = true;
		} else {
			for (int i = 0; i < HASH_COUNT; i++) {
				int index = entry[recordSize + i];
				boolean occupied = false;
				for (int j = index + recordSize - 2; j >= index; j -= 2) {
					if (map[j] != -1 || map[j+1] != -1) {
						occupied = true;
						break;
					}
				}
				if (!occupied) {
					System.arraycopy(entry, 0, map, index, entrySize);
					return;
				}
			}
			int[] other = entry == first ? second : first;
			int i = random.nextInt(HASH_COUNT);
			int index = entry[recordSize + i];
			System.arraycopy(map, index, other, 0, entrySize);
			System.arraycopy(entry, 0, map, index, entrySize);
			//TODO remove recursion, this may blow the stack
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
				hashStats.positionBits = 64 - Long.numberOfLeadingZeros(largestPosition + 1);
				hashStats.ordinalBits = 64 - Long.numberOfLeadingZeros(largestOrdinal + 1);
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
		recStats = null;
		factory = null;
		first = null;
		second = null;
		checker = null;
		map = null;
	}

	private void writeFile() {
		CodedStreams.writeToFile(new CodedStreams.WriteTask() {
			@Override
			public void writeTo(CodedWriter writer) {
				final BitWriter w = writer.getWriter();
				final int[] map = HashConsumer.this.map;
				final HashStats stats = hashStats;
				for (int i = 0; i < map.length; i += entrySize) {
					int j = i;
					//TODO could optimize by only doing int if bits <= 32
					if (stats.positional) {
						long v1 = ((long) map[j++]) << 32;
						long v2 = (map[j++] & 0x00000000ffffffffL);
						w.write(v1 | v2, hashStats.positionBits);
					}
					if (stats.ordinal) {
						long v1 = ((long) map[j++]) << 32;
						long v2 = (map[j++] & 0x00000000ffffffffL);
						w.write(v1 | v2, hashStats.ordinalBits);
					}
				}
			}
		}, hashStats.coding, file);
	}
	
}
