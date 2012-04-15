/*
 * Copyright 2007 Tom Gibara
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

import java.awt.image.BufferedImage;

import com.tomgibara.graphics.util.ImageUtil.IntensityModel;

public class GrayLevelSplitter {

	public static BufferedImage[] split(BufferedImage src, int threshold) {

		final int width = src.getWidth();
		final int height = src.getHeight();
		final int size = width * height;
		
		final byte[] value = ImageUtil.toByteIntensity(src, IntensityModel.AVERAGE);
		final byte[] alpha = ImageUtil.extractAlpha(src);

		if (threshold < 0) {
			int[] counts = new int[256];
			for (int i = 0; i < size; i++) {
				counts[value[i] & 0xff] += alpha[i] & 0xff;
			}
			threshold = 0;
			for (int i = 1; i < counts.length; i++) {
				if (counts[i] > counts[threshold]) threshold = i;
			}
		}
		
		final int[] darkData = new int[size];
		final int[] liteData = new int[size];
		
		for (int i = 0; i < size; i++) {
			final int v = value[i] & 0xff;
			final int a = alpha[i] & 0xff;
			final int c = (v << 16) | (v << 8) | v;
			if (v < threshold) {
				liteData[i] = c;
				int na = a * (threshold - v) / threshold;
				darkData[i] = 0x00000000 | (na << 24);
			} else {
				darkData[i] = c;
				int na = a * (v - threshold) / (255 - threshold);
				liteData[i] = 0x00ffffff | (na << 24);
			}
		}
		
		BufferedImage dark = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage lite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		dark.getRaster().setDataElements(0, 0, width, height, darkData);
		lite.getRaster().setDataElements(0, 0, width, height, liteData);
		
		return new BufferedImage[] { dark, lite };
	}

	public static BufferedImage[] split(BufferedImage src) {
		return split(src, -1);
	}

}
