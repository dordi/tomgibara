/*
 * Created on 31-Jan-2007
 */
package com.tomgibara.crinch.coding;

import java.util.Arrays;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.BitDumper;
import com.tomgibara.crinch.bits.MemoryBitReader;
import com.tomgibara.crinch.bits.MemoryBitWriter;


public class HuffmanTest extends TestCase {
    
    public static void main(String[] args) {
        testDecode(new long[] {10, 15, 30, 16, 29});
        testDecode(new long[] {20, 20, 20, 20, 20});
        testDecode(new long[] {0, 10, 20, 30, 40, 60});
    }

    public void testEncode() {
      test(new long[] {10, 15, 30, 16, 29});
      test(new long[] {20, 20, 20, 20, 20});
      test(new long[] {10, 20, 30, 40, 60});
      test(new long[] {0, 10, 20, 30, 40, 60});
      test(new long[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,100});
    }
    
    public void testDecode() {
        testDecode(new long[] {10, 15, 30, 16, 29});
        testDecode(new long[] {20, 20, 20, 20, 20});
        testDecode(new long[] {0, 10, 20, 30, 40, 60});
    }

    private static void testDecode(long[] freqs) {

        descendingSort(freqs);
        Huffman huffman = new Huffman(freqs);
		int[] memory = new int[1000];
		MemoryBitWriter w = new MemoryBitWriter(memory, 1000*8, 0);
		BitDumper d = new BitDumper();
		for (int i = 1; i <= freqs.length; i++) {
			huffman.encode(i, w);
			huffman.encode(i, d);
		}
		w.flush();
		System.out.println();
		
		MemoryBitReader r = new MemoryBitReader(memory, 1000*8, 0);
		for (int i = 1; i <= freqs.length; i++) {
			int j = huffman.decode(r);
			if (j != i) throw new IllegalStateException(j + " != " + i);
		}
        
	}

	public void testUneven() {
        int size = 1000;
        long[] uneven = new long[size];
        for (int i = 0; i < uneven.length; i++) {
            uneven[i] = size - i;
        }
        Huffman huffman = new Huffman(uneven);
        System.out.println("==================");
        BitDumper dumper = new BitDumper();
        for (int i = size - 100; i <= size; i++) {
            System.out.print("(" + i + ") ");
            huffman.encode(i, dumper);
            dumper.flush();
        }
        System.out.println();
        
    }
    
    private static void test(long[] freqs) {

        descendingSort(freqs);
        
        Huffman huffman = new Huffman(freqs);

        System.out.println("==================");
        BitDumper dumper = new BitDumper();
        for (int i = 1; i <= freqs.length; i++) {
            huffman.encode(i, dumper);
            dumper.flush();
        }
        System.out.println();
    }

    private static void descendingSort(long[] a) {
        Arrays.sort(a);
        for(int i=0;i<a.length/2;i++){
            long t = a[i];
            a[i] = a[a.length-(1+i)];
            a[a.length-(1+i)] = t;
        }
    }
    
}