/*
 * Copyright 2005 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.graphics.util;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class IncComposite implements Composite {

    int brightest = 0;
    
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        if (dstColorModel.getNumComponents() != 1) throw new IllegalArgumentException();
        return new CompositeContext() {
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                int minX = Math.max(src.getMinX(), Math.max(dstIn.getMinX(), dstOut.getMinX()));
                int minY = Math.max(src.getMinY(), Math.max(dstIn.getMinY(), dstOut.getMinY()));
                int maxX = Math.min(src.getMinX()+src.getWidth(), Math.min(dstIn.getMinX()+dstIn.getWidth(), dstOut.getMinX()+dstOut.getWidth()));
                int maxY = Math.min(src.getMinY()+src.getHeight(), Math.min(dstOut.getMinY()+dstIn.getHeight(), dstOut.getMinY()+dstOut.getHeight()));
                int[] dstValues = dstIn.getPixels(minX, minY, maxX-minX, maxY-minY, (int[])null);
                for (int i = 0; i < dstValues.length; i++) {
                    if (++dstValues[i] > brightest) brightest = dstValues[i];
                    
                }
                dstOut.setPixels(minX, minY, maxX-minX, maxY-minY, dstValues);
            }
            
            public void dispose() {
            }
        };
    }
    
    public int getBrightest() {
		return brightest;
	}
    
    public void setBrightest(int brightest) {
		this.brightest = brightest;
	}
    
}