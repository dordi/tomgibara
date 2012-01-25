/*
 * Copyright 2007 Tom Gibara
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
package com.tomgibara.crinch.coding;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.PriorityQueue;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreamException;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitWriter;

public class HuffmanCoding implements Coding {

	// statics

	public interface Correspondence {
		
		int getCount();
		
		int getIndex(int value) throws IllegalArgumentException;
		
		int getValue(int index) throws BitStreamException;
		
	}
	
	public interface Frequencies {
		
		int getCount();
		
		long getFrequency(int index);
		
		Correspondence getCorrespondence();
		
	}
	
	private static class DirectCorrespondence implements Correspondence {

		private final int count;
		
		DirectCorrespondence(int count) {
			this.count = count;
		}
		
		@Override
		public int getCount() {
			return count;
		}
		
		@Override
		public int getIndex(int value) {
			if (value <= 0) throw new IllegalArgumentException("non-positive value");
			if (value > count) throw new IllegalArgumentException("invalid value");
			return value - 1;
		}
		
		@Override
		public int getValue(int index) {
			if (index >= count) throw new BitStreamException("invalid huffman encoding: " + index);
			return index + 1;
		}
		
	}
	
	private static class DenseCorrespondence implements Correspondence {
		
		private final int[] values;
		private final int[] indices;

		DenseCorrespondence(int[] values, int[] indices) {
			this.values = values;
			this.indices = indices;
		}
		
		@Override
		public int getCount() {
			return values.length;
		}
		
		@Override
		public int getIndex(int value) {
			if (value <= 0) throw new IllegalArgumentException("non-positive value");
			if (value > indices.length) throw new IllegalArgumentException("invalid value: " + value);
			int index = indices[value - 1];
			if (index < 0) throw new IllegalArgumentException("invalid value: " + value);
			return index;
		}
		
		public int getValue(int index) {
			if (index >= values.length) throw new BitStreamException("invalid huffman encoding: " + index);
			int value = values[index];
			if (value < 0) throw new BitStreamException("invalid huffman encoding: " + index);
			return value;
		}
		
	}
	
	public static class DescendingFrequencyValues implements Frequencies {
		
		private final long[] frequencies;

		public DescendingFrequencyValues(long[] frequencies) {
			if (frequencies == null) throw new IllegalArgumentException("null frequencies");
			this.frequencies = frequencies;
		}

		@Override
		public int getCount() {
			return frequencies.length;
		}
		
		@Override
		public long getFrequency(int index) {
			return frequencies[index];
		}
		
		@Override
		public Correspondence getCorrespondence() {
			return new DirectCorrespondence(frequencies.length);
		}

	}

	public static class UnorderedFrequencyValues implements Frequencies {

		private final long[] frequencies;
		private final Correspondence correspondence;
		
		public UnorderedFrequencyValues(long[] frequencies) {
			if (frequencies == null) throw new IllegalArgumentException("null frequencies");
			int count = frequencies.length;
			El[] els = new El[count];
			for (int i = 0; i < count; i++) {
				els[i] = new El(i, frequencies[i]);
			}
			Arrays.sort(els);
			int limit;
			for (limit = 0; limit < count; limit++) {
				if (els[limit].freq == 0) break;
			}
			
			frequencies = new long[limit];
			//TODO support 'non-dense' lookups
			int[] values = new int[count];
			int[] indices = new int[count];
			for (int i = 0; i < count; i++) {
				El el = els[i];
				if (i < limit) {
					frequencies[i] = el.freq;
					int j = el.index;
					values[i] = j + 1;
					indices[j] = i;
				} else {
					values[i] = -1;
					indices[el.index] = -1;
				}
			}
			
			this.frequencies = frequencies;
			correspondence = new DenseCorrespondence(values, indices);
		}
		
		@Override
		public int getCount() {
			return frequencies.length;
		}
		
		@Override
		public long getFrequency(int index) {
			return frequencies[index];
		}
		
		@Override
		public Correspondence getCorrespondence() {
			return correspondence;
		}
		
		private static class El implements Comparable<El> {
			
			int index;
			long freq;
			
			El(int index, long freq) {
				this.index = index;
				this.freq = freq;
			}
			
			@Override
			public int compareTo(El that) {
				if (this.freq == that.freq) return 0;
				return this.freq < that.freq ? 1 : -1;
			}
			
		}

	}
	
    private static Node buildTree(PriorityQueue<Node> nodes) {
        while(true) {
            Node node1 = nodes.poll();
            Node node2 = nodes.poll();
            if (node2 == null) return node1;
            Node node = new Node(node1, node2);
            nodes.add(node);
        }
    }
    
    private static Node[] createNodes(Frequencies frequencies) {
        final int count = frequencies.getCount();
        PriorityQueue<Node> nodes = new PriorityQueue<Node>(count);
        Node[] array = new Node[count];
        long lastFreq = 0L;
        for (int i = 0; i < count; i++) {
        	long freq = frequencies.getFrequency(i);
        	if (i > 0 && lastFreq < freq) throw new IllegalArgumentException("frequencies not descending at index " + i);
            Node leaf = new Node(freq);
            array[i] = leaf;
            nodes.add(leaf);
        	lastFreq = freq;
        }
        buildTree(nodes);
        return array;
    }

    private static int[] calculateLengths(Node[] nodes) {
        int[] lengths = new int[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            lengths[i] = nodes[i].getLength();
        }
        return lengths;
    }
    
    private static int[] countLengths(int[] lengths) {
        int maxLen = 0;
        int[] count = new int[16];
        for (int length : lengths) {
            if (length >= maxLen) {
                if (length >= count.length) {
                    int newLen = Math.max(length + 1, 2 * count.length);
                    int[] tmp = new int[newLen];
                    System.arraycopy(count, 0, tmp, 0, maxLen);
                    count = tmp;
                }
                maxLen = length;
            }
            count[length] ++;
        }
        int[] ret = new int[maxLen + 1];
        System.arraycopy(count, 0, ret, 0, maxLen+1);
        return ret;
    }

    private static int[] accumulateCounts(int[] counts) {
        int[] cumm = new int[counts.length];
        int c = 0;
        for (int i = 0; i < counts.length; i++) {
            c += counts[i];
            cumm[i] = c;
        }
        return cumm;
    }
    
    private static int[] encodeCounts(int[] counts) {
        int[] codes = new int[counts.length];
        int l = 0;
        for (int i = 1; i < counts.length; i++) {
            int count = counts[i];
            if (count == 0) continue;
            codes[i] = (codes[l] + counts[l]) << (i - l);
            l = i;
        }
        return codes;
    }
    
    // fields
    
    private final Correspondence correspondence;
    private final int symbols;
    private final int[] counts;
    private final int[] codes;
    private final int[] cumm;
    private final Nid root;
    
    // constructors
    
    public HuffmanCoding(Frequencies frequencies) {
    	if (frequencies == null) throw new IllegalArgumentException("null frequencies");
    	correspondence = frequencies.getCorrespondence();
    	symbols = frequencies.getCount();
        Node [] nodes = createNodes(frequencies);
        int[] lengths = calculateLengths(nodes);
        counts = countLengths(lengths);
        codes = encodeCounts(counts);
        cumm = accumulateCounts(counts);
        root = produceNids();
        root.computeLeastHeights();
        root.computeLookups();
    }

    // methods
    
    public int getCodeLength(int value) {
    	return getCodeLengthForIndex(correspondence.getIndex(value));
    }
    
    // coding methods
    
    @Override
    public int encodePositiveInt(BitWriter writer, int value) {
    	return unsafeEncodePositiveInt(writer, value);
    }
    
    @Override
    public int encodePositiveLong(BitWriter writer, long value) {
    	return unsafeEncodePositiveInt(writer, (int) value);
    }
    
    @Override
    public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
    	if (value.compareTo(BigInteger.valueOf(correspondence.getCount())) > 0) throw new IllegalArgumentException("value exceeds number of symbols");
    	return unsafeEncodePositiveInt(writer, value.intValue());
    }
    
    public int decodePositiveInt(BitReader r) {
    	Nid nid = root;
		while (nid.index == -1) {
			// simple but slower, using lookup is about 25% faster overall
    		//nid = r.readBoolean() ? nid.one: nid.zero;
			nid = nid.lookup[ r.read(nid.leastHeight) ];
		}
	    return correspondence.getValue(nid.index);
    }
    
    @Override
    public long decodePositiveLong(BitReader reader) {
    	return decodePositiveInt(reader);
    }
    
    @Override
    public BigInteger decodePositiveBigInt(BitReader reader) {
    	return BigInteger.valueOf(decodePositiveInt(reader));
    }
    
    private int unsafeEncodePositiveInt(BitWriter writer, int value) {
		return encodeIndex(writer, correspondence.getIndex(value));
    }
    
    private int encodeIndex(BitWriter writer, int index) {
    	int x = getCodeLengthForIndex(index);
        writer.write(codes[x] + index - cumm[x - 1], x);
        return x;
    }
    
    private int getCodeLengthForIndex(int index) {
        int x = Arrays.binarySearch(cumm, index + 1);
        //we may not have an exact match
        if (x < 0) x = - 1 - x;
        
        //check value
        if (x == 0) throw new IllegalArgumentException();
        //if (x == cumm.length) throw new IllegalArgumentException();
        //TODO what's the fix?
        if (x == cumm.length) x--;
        
        //we may have missing lengths == dups in cumm
        while( x > 1 && cumm[x] == cumm[x - 1] ) x--;
        
        return x;
    }

    private Nid produceNids() {
    	int size = (codes.length+31)/32;
    	int[] mem = new int[size];
    	IntArrayBitWriter w = new IntArrayBitWriter(mem, size*32);
    	IntArrayBitReader r = new IntArrayBitReader(mem, size*32);
    	Nid root = new Nid();
    	for (int i = 0; i < symbols; i++) {
        	w.setPosition(0);
        	w.flush();
        	encodeIndex(w, i);
        	int length = (int) w.getPosition();
        	r.setPosition(0);
        	Nid nid = root;
        	for (int j = 0; j < length; j++) {
            	int bit = r.readBit();
            	Nid n;
				if (bit == 0) {
					n = nid.zero;
					if (n == null) {
						n = new Nid();
						nid.zero = n;
					}
				} else {
					n = nid.one;
					if (n == null) {
						n = new Nid();
						nid.one = n;
					}
				}
				nid = n;
			}
        	nid.index = i;
		}
    	return root;
    }

    private static class Node implements Comparable<Node> {

        final long freq;
        int length;
        Node parent;
        
        Node(long freq) {
            this.freq = freq;
            this.length = -1;
        }
        
        Node(Node left, Node right) {
            left.parent = this;
            right.parent = this;
            freq = left.freq + right.freq;
            length = 0;
        }
        
        int getLength() {
            if (length <= 0) {
                length = parent == null ? 0 : parent.getLength() + 1;
            }
            return length;
        }
        
        public int compareTo(Node that) {
            if (this.freq < that.freq) return -1;
            if (this.freq > that.freq) return 1;
            if (this.length < that.length) return -1;
            if (this.length > that.length) return 1;
            return 0;
        }

    }

    private static class Nid {

        int index = -1;
        Nid zero;
        Nid one;
        int leastHeight;
        Nid[] lookup;

        void computeLeastHeights() {
        	if (zero == null || one == null) {
        		leastHeight = 0;
        	} else {
	        	one.computeLeastHeights();
	        	zero.computeLeastHeights();
	        	leastHeight = 1 + Math.min(zero.leastHeight, one.leastHeight);
        	}
        }

        void computeLookups() {
        	if (leastHeight == 0) return;
        	int length = 1 << leastHeight;
        	lookup = new Nid[length];
        	for (int i = 0; i < length; i++) {
        		Nid n = this;
				for (int b = leastHeight - 1; b >= 0; b--) {
					n = ((i >> b) & 1) == 0 ? n.zero : n.one; 
				}
				n.computeLookups();
				lookup[i] = n;
			}
        }
        
		@Override
		public String toString() {
			//return index == -1 ? "(zero: " + zero + "  one: " + one + ")" : Integer.toString(index);
			return lookup == null ? index == -1 ? "(zero: " + zero + "  one: " + one + ")" : Integer.toString(index) : Arrays.toString(lookup);
		}

    }
    
}
