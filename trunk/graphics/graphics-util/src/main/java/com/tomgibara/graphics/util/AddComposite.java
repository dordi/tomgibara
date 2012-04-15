/**
 * 
 */
package com.tomgibara.graphics.util;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class AddComposite implements Composite {
    
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        if (dstColorModel.getNumComponents() != 1) throw new IllegalArgumentException();
        if (srcColorModel.getTransferType() != DataBuffer.TYPE_INT) throw new IllegalArgumentException();
        return new CompositeContext() {
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                int minX = Math.max(src.getMinX(), Math.max(dstIn.getMinX(), dstOut.getMinX()));
                int minY = Math.max(src.getMinY(), Math.max(dstIn.getMinY(), dstOut.getMinY()));
                int maxX = Math.min(src.getMinX()+src.getWidth(), Math.min(dstIn.getMinX()+dstIn.getWidth(), dstOut.getMinX()+dstOut.getWidth()));
                int maxY = Math.min(src.getMinY()+src.getHeight(), Math.min(dstOut.getMinY()+dstIn.getHeight(), dstOut.getMinY()+dstOut.getHeight()));
                int[] srcValues = src.getSamples(minX, minY, maxX-minX, maxY-minY, 0, (int[])null);
                int[] dstValues = dstIn.getPixels(minX, minY, maxX-minX, maxY-minY, (int[])null);
                for (int i = 0; i < dstValues.length; i++) {
                    dstValues[i] += 256 * srcValues[i];
                    
                }
                dstOut.setPixels(minX, minY, maxX-minX, maxY-minY, dstValues);
            }
            
            public void dispose() {
            }
        };
    }
    
}