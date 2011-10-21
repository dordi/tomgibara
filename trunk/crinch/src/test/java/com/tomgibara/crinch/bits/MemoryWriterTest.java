/*
 * Created on 29-Jan-2007
 */
package com.tomgibara.crinch.bits;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import junit.framework.TestCase;

public class MemoryWriterTest extends TestCase {

    public void testPass() {
        int size = 1000;
        for (long seed = 0; seed < 10; seed++) {
            testPass(size, seed);
        }
    }
    
    private void testPass(int size, long seed) {
        int[] mem = new int[size];
        MemoryBitWriter writer = new MemoryBitWriter(mem, size * 32, 0);
        ArrayList<Point> list = new ArrayList<Point>(size);
        
        Random r = new Random(1);
        for (int i = 0; i < size; i++) {
            int x = r.nextInt(33);
            int y = x == 0 ? 0 : r.nextInt(x);
            writer.write(y, x);
            list.add( new Point(x, y) );
        }
        long pos = writer.getPosition();
        
        MemoryBitReader reader = new MemoryBitReader(mem, size * 32, 0);
        for (int i = 0; i < size; i++) {
            Point pt = list.get(i);
            int v = reader.read(pt.x);
            if (pt.y != v) throw new RuntimeException("Failed at " + i + ": " + v + " is not " + pt.y);
        }
        if (reader.getPosition() != pos) throw new RuntimeException();
    }

    public void testRuns() {
        int size = 1000;
        for (long seed = 0; seed < 10; seed++) {
            testRuns(size, seed);
        }
    }
    
    private void testRuns(int size, long seed) {
        int maxrunlength = 100;
        int asize = size * maxrunlength * 2;
        int[] mem = new int[asize];
        MemoryBitWriter writer = new MemoryBitWriter(mem, asize, 0);
        ArrayList<Point> list = new ArrayList<Point>(size);
        
        Random r = new Random(1);
        for (int i = 0; i < size; i++) {
            int x = r.nextInt(maxrunlength);
            int y = r.nextInt(maxrunlength);
            writer.writeZeros(x);
            writer.writeOnes(y);
            list.add( new Point(x, y) );
        }
        long pos = writer.getPosition();
        
        MemoryBitReader reader = new MemoryBitReader(mem, asize, 0);
        for (int i = 0; i < size; i++) {
            Point pt = list.get(i);
            for(int x = 0; x < pt.x; x++) {
                if (reader.readBit() != 0) throw new RuntimeException("Failed at " + i + ": expected 0");
            }
            for(int y = 0; y < pt.y; y++) {
                int v = reader.readBit();
                if (v != 1) throw new RuntimeException("Failed at " + i + ": expected 1, got " + v);
            }
        }
        if (reader.getPosition() != pos) throw new RuntimeException();
    }

}
