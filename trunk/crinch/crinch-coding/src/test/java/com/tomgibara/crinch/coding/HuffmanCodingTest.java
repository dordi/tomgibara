/*
 * Created on 31-Jan-2007
 */
package com.tomgibara.crinch.coding;

import java.util.Arrays;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.BitStreams;
import com.tomgibara.crinch.bits.PrintStreamBitWriter;
import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitWriter;


public class HuffmanCodingTest extends TestCase {
    
    public void testEncode() {
      test(new long[] {10, 15, 30, 16, 29});
      test(new long[] {20, 20, 20, 20, 20});
      test(new long[] {10, 20, 30, 40, 60});
      test(new long[] {0, 10, 20, 30, 40, 60});
      test(new long[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,100});
    }
    
    public void testDecode() {
    	testDecodeBoth(new long[] {10, 15, 30, 16, 29});
    	testDecodeBoth(new long[] {20, 20, 20, 20, 20});
    	testDecodeBoth(new long[] {10, 20, 30, 40, 60});
    	testDecodeBoth(new long[] {1, 2, 4, 8, 16});
    	testDecodeBoth(new long[] {1, 1});
    	testDecodeBoth(new long[] {1, 1000});
    }

    private static void testDecodeBoth(long[] freqs) {
    	final HuffmanCoding.UnorderedFrequencyValues f1 = new HuffmanCoding.UnorderedFrequencyValues(freqs);
        descendingSort(freqs);
        final HuffmanCoding.DescendingFrequencyValues f2 = new HuffmanCoding.DescendingFrequencyValues(freqs);
        assertEquals(f1.getCorrespondence().getCount(), f2.getCorrespondence().getCount());
        int count = f1.getCorrespondence().getCount();
        for (int i = 0; i < count; i++) {
			assertEquals("mismatched frequencies at index " + i, f1.getFrequency(i), f2.getFrequency(i));
		}
        
		BitReader r1 = testDecode(f1);
		BitReader r2 = testDecode(f2);

		// can't assert this because values are effectively written out in a different order
        //assertTrue(BitStreams.isSameBits(r1, r2));
    }

    private static BitReader testDecode(HuffmanCoding.Frequencies frequencies) {
        HuffmanCoding huffman = new HuffmanCoding(frequencies);
		int[] memory = new int[1000];
		IntArrayBitWriter w = new IntArrayBitWriter(memory);
		PrintStreamBitWriter d = new PrintStreamBitWriter();
		int count = frequencies.getCorrespondence().getCount();
		for (int i = 1; i <= count; i++) {
			huffman.encodePositiveInt(w, i);
			huffman.encodePositiveInt(d, i);
		}
		w.flush();
		System.out.println();
		
		IntArrayBitReader r = new IntArrayBitReader(memory);
		for (int i = 1; i <= count; i++) {
			assertEquals(i, huffman.decodePositiveInt(r));
		}
		
        r.setPosition(0);
        return r;
	}

	public void testUneven() {
        int size = 1000;
        long[] uneven = new long[size];
        for (int i = 0; i < uneven.length; i++) {
            uneven[i] = size - i;
        }
        HuffmanCoding huffman = new HuffmanCoding(new HuffmanCoding.DescendingFrequencyValues(uneven));
        System.out.println("==================");
        PrintStreamBitWriter dumper = new PrintStreamBitWriter();
        for (int i = size - 100; i <= size; i++) {
            System.out.print("(" + i + ") ");
            huffman.encodePositiveInt(dumper, i);
            dumper.flush();
        }
        System.out.println();
        
    }
    
    private static void test(long[] freqs) {

        descendingSort(freqs);
        
        HuffmanCoding huffman = new HuffmanCoding(new HuffmanCoding.DescendingFrequencyValues(freqs));

        System.out.println("==================");
        PrintStreamBitWriter dumper = new PrintStreamBitWriter();
        for (int i = 1; i <= freqs.length; i++) {
            huffman.encodePositiveInt(dumper, i);
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
