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
import java.util.List;
import java.util.Map.Entry;

import com.tomgibara.cluster.gvm.GvmResult;
import com.tomgibara.cluster.gvm.space.GvmVectorSpace;

public class ClusterPoints {

	public static void main(String[] args) throws IOException {
		for (Entry<String, Integer> entry : ClusterFiles.files.entrySet()) {
			cluster(entry.getKey(), entry.getValue());
		}
	}

	private static void cluster(String name, int capacity) throws IOException {

		final List<GvmResult<GvmVectorSpace, List<double[]>>> results = ClusterFiles.cluster(name, capacity);
			
		FileWriter writer = new FileWriter("../cluster-common/R/" + name + "-clustered.txt");
		for (int i = 0; i < results.size(); i++) {
			for (double[] pt : results.get(i).getKey()) {
				writer.write(String.format("%3.3f %3.3f %d%n", pt[0], pt[1], i+1));
			}
		}
		writer.close();
	}

}
