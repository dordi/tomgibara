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

import java.util.Arrays;

import junit.framework.TestCase;

import com.tomgibara.crinch.bits.BitBoundary;
import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.ByteArrayBitReader;
import com.tomgibara.crinch.bits.ByteArrayBitWriter;
import com.tomgibara.crinch.bits.IntArrayBitReader;
import com.tomgibara.crinch.bits.IntArrayBitWriter;
import com.tomgibara.crinch.coding.HuffmanCoding.DescendingFrequencyValues;
import com.tomgibara.crinch.coding.HuffmanCoding.Dictionary;
import com.tomgibara.crinch.coding.HuffmanCoding.UnorderedFrequencyValues;


public class HuffmanCodingTest extends TestCase {
    
//    public void testEncode() {
//      test(new long[] {10, 15, 30, 16, 29});
//      test(new long[] {20, 20, 20, 20, 20});
//      test(new long[] {10, 20, 30, 40, 60});
//      test(new long[] {0, 10, 20, 30, 40, 60});
//      test(new long[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,100});
//    }
    
    public void testDecode() {
    	testDecodeBoth(new long[] {10, 15, 30, 16, 29});
    	testDecodeBoth(new long[] {20, 20, 20, 20, 20});
    	testDecodeBoth(new long[] {10, 20, 30, 40, 60});
    	testDecodeBoth(new long[] {1, 2, 4, 8, 16});
    	testDecodeBoth(new long[] {1, 1});
    	testDecodeBoth(new long[] {1, 1000});
    	testDecodeBoth(new long[] {10});
    }

    public void testDictionary() {
    	HuffmanCoding coding = new HuffmanCoding(new UnorderedFrequencyValues(9L, 16L, 25L, 36L));
    	int[] sequence = {1,2,3,4,3,2,1,4,3,2,1};
    	byte[] bytes = new byte[11];
    	ByteArrayBitWriter writer = new ByteArrayBitWriter(bytes);
    	for (int i = 0; i < sequence.length; i++) {
			coding.encodePositiveInt(writer, sequence[i]);
		}
    	writer.flush();
    	Dictionary dictionary = coding.getDictionary();
    	coding = new HuffmanCoding(dictionary);
    	ByteArrayBitReader reader = new ByteArrayBitReader(bytes);
    	for (int i = 0; i < sequence.length; i++) {
    		assertEquals("mismatch at index " + i, sequence[i], coding.decodePositiveInt(reader));
    	}
    }
    
    private static void testDecodeBoth(long[] freqs) {
    	final UnorderedFrequencyValues f1 = new UnorderedFrequencyValues(freqs);
        descendingSort(freqs);
        final DescendingFrequencyValues f2 = new DescendingFrequencyValues(freqs);
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
		int count = frequencies.getCorrespondence().getCount();
		IntArrayBitWriter w = new IntArrayBitWriter(memory);
//		PrintStreamBitWriter d = new PrintStreamBitWriter();
		for (int i = 1; i <= count; i++) {
			huffman.encodePositiveInt(w, i);
//			huffman.encodePositiveInt(d, i);
		}
		w.flush();
//		System.out.println();
		
		IntArrayBitReader r = new IntArrayBitReader(memory);
		for (int i = 1; i <= count; i++) {
			assertEquals(i, huffman.decodePositiveInt(r));
		}
		
        r.setPosition(0);
        return r;
	}

//TODO make this a useful test
//	public void testUneven() {
//        int size = 1000;
//        long[] uneven = new long[size];
//        for (int i = 0; i < uneven.length; i++) {
//            uneven[i] = size - i;
//        }
//        HuffmanCoding huffman = new HuffmanCoding(new HuffmanCoding.DescendingFrequencyValues(uneven));
//        System.out.println("==================");
//        PrintStreamBitWriter dumper = new PrintStreamBitWriter();
//        for (int i = size - 100; i <= size; i++) {
//            System.out.print("(" + i + ") ");
//            huffman.encodePositiveInt(dumper, i);
//            dumper.flush();
//        }
//        System.out.println();
//        
//    }
    
  //TODO make this a useful test
//    private static void test(long[] freqs) {
//
//        descendingSort(freqs);
//        
//        HuffmanCoding huffman = new HuffmanCoding(new HuffmanCoding.DescendingFrequencyValues(freqs));
//
//        System.out.println("==================");
//        PrintStreamBitWriter dumper = new PrintStreamBitWriter();
//        for (int i = 1; i <= freqs.length; i++) {
//            huffman.encodePositiveInt(dumper, i);
//            dumper.flush();
//        }
//        System.out.println();
//    }

    private static void descendingSort(long[] a) {
        Arrays.sort(a);
        for(int i=0;i<a.length/2;i++){
            long t = a[i];
            a[i] = a[a.length-(1+i)];
            a[a.length-(1+i)] = t;
        }
    }
    
}
