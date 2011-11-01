/*
 * Created on 31-Jan-2007
 */
package com.tomgibara.crinch.coding;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.PriorityQueue;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;

public class HuffmanCoding implements Coding {

	// statics
	
    private static Node buildTree(PriorityQueue<Node> nodes) {
        while(true) {
            Node node1 = nodes.poll();
            Node node2 = nodes.poll();
            if (node2 == null) return node1;
            Node node = new Node(node1, node2);
            nodes.add(node);
        }
    }
    
    private static Node[] createNodes(long[] freqs) {
        PriorityQueue<Node> nodes = new PriorityQueue<Node>(freqs.length);
        Node[] array = new Node[freqs.length];
        for (int i = 0; i < freqs.length; i++) {
            Node leaf = new Node(freqs[i], i+1);
            array[i] = leaf;
            nodes.add(leaf);
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
    
    private final int symbols;
    private final int[] counts;
    private final int[] codes;
    private final int[] cumm;
    private final Nid root;
    
    // constructors
    
    //NOTE: supplied array must have already been sorted in descending order
    public HuffmanCoding(long[] freqs) {
    	symbols = freqs.length;
        Node [] nodes = createNodes(freqs);
        int[] lengths = calculateLengths(nodes);
        counts = countLengths(lengths);
        codes = encodeCounts(counts);
        cumm = accumulateCounts(counts);
        root = produceNids();
    }

    // methods
    
    public int getCodeLength(int value) {
        int x = Arrays.binarySearch(cumm, value);
        
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
    
    // coding methods
    
    @Override
    public int encodePositiveInt(BitWriter writer, int value) {
    	if (value <= 0) throw new IllegalArgumentException("non-positive value");
    	if (value > symbols) throw new IllegalArgumentException("value exceeds number of symbols");
    	return unsafeEncodePositiveInt(writer, value);
    }
    
    @Override
    public int encodePositiveLong(BitWriter writer, long value) {
    	if (value <= 0) throw new IllegalArgumentException("non-positive value");
    	if (value > symbols) throw new IllegalArgumentException("value exceeds number of symbols");
    	return unsafeEncodePositiveInt(writer, (int) value);
    }
    
    @Override
    public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
    	if (value == null) throw new IllegalArgumentException("null value");
    	if (value.signum() < 1) throw new IllegalArgumentException("non-positive value");
    	if (value.compareTo(BigInteger.valueOf(symbols)) > 0) throw new IllegalArgumentException("value exceeds number of symbols");
    	return unsafeEncodePositiveInt(writer, value.intValue());
    }
    
    //TODO investigate possibilities for optimization:
    //eg reading minimum number of bits available
    public int decodePositiveInt(BitReader r) {
    	Nid nid = root;
    	while(nid.value == 0) {
    		nid = r.readBoolean() ? nid.one: nid.zero;
    	}
    	return nid.value;
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
    	int x = getCodeLength(value);
        writer.write(codes[x] + value - cumm[x - 1] - 1, x);
        return x;
    }
    
    private Nid produceNids() {
    	int size = (codes.length+31)/32;
    	int[] mem = new int[size];
    	MemoryBitWriter w = new MemoryBitWriter(mem, size*32, 0);
    	MemoryBitReader r = new MemoryBitReader(mem, size*32, 0);
    	Nid root = new Nid();
    	for (int i = 1; i <= symbols; i++) {
        	w.setPosition(0);
        	w.flush();
        	unsafeEncodePositiveInt(w, i);
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
        	nid.value = i;
		}
    	return root;
    }

    private static class Node implements Comparable<Node> {

        final long freq;
        final int value;
        int length;
        Node parent;
        
        Node(long freq, int value) {
            this.freq = freq;
            this.value = value;
            this.length = -1;
        }
        
        Node(Node left, Node right) {
            left.parent = this;
            right.parent = this;
            freq = left.freq + right.freq;
            this.value = 0;
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

        int value;
        Nid zero;
        Nid one;

		@Override
		public String toString() {
			return value == 0 ? "(zero: " + zero + "  one: " + one + ")" : Integer.toString(value);
		}

    }
    
}
