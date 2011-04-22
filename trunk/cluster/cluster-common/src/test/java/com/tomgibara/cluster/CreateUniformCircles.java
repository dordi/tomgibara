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
package com.tomgibara.cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.NormalizedRandomGenerator;
import org.apache.commons.math.random.UncorrelatedRandomVectorGenerator;
import org.apache.commons.math.random.UniformRandomGenerator;

public class CreateUniformCircles {

	public static void main(String[] args) throws IOException {
		UniformRandomGenerator gen = new UniformRandomGenerator(new JDKRandomGenerator());
		FileWriter writer = new FileWriter("R/circles.txt");
		int size = 400;
		try {
			for (int y = 4; y >= -4; y--) {
				for (int x = -4; x <= 4; x++) {
					writeCluster(gen, new double[] {x, y}, new double[] {0.3, 0.3}, size, writer);
				}
			}
		} finally {
			writer.close();
		}
	}

	private static void writeCluster(NormalizedRandomGenerator gen, double[] means, double[] deviations, int size, Writer writer) throws IOException {
		UncorrelatedRandomVectorGenerator c = new UncorrelatedRandomVectorGenerator(means, deviations, gen);
		int count = 0;
		while (count < size) {
			double[] pt = c.nextVector();
			double x = pt[0] - means[0];
			double y = pt[1] - means[1];
			if (x*x+y*y>deviations[0]*deviations[1]) continue;
			writer.write(String.format("%3.3f %3.3f%n", pt[0], pt[1]));
			count++;
		}
	}
	
}
