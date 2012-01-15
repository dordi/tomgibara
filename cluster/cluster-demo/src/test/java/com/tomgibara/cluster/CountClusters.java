package com.tomgibara.cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.tomgibara.cluster.gvm.GvmResult;
import com.tomgibara.cluster.gvm.space.GvmVectorSpace;


public class CountClusters {

	public static final int MAX_CAPACITY = 10;
	
	public static void main(String[] args) throws IOException {
		for (Entry<String, Integer> entry : ClusterFiles.files.entrySet()) {
			countClusters(entry.getKey(), entry.getValue());
		}
	}

	private static void countClusters(String name, int clusterCount) throws IOException {
		if (clusterCount >= MAX_CAPACITY) return; //skip
		List<double[]> pts = ClusterFiles.read(name);
		FileWriter writer = new FileWriter("../cluster-common/R/" + name + "-variances.txt");
		for (int capacity = 1; capacity < MAX_CAPACITY; capacity++) {
			final List<GvmResult<GvmVectorSpace, Void>> results = ClusterFiles.clusterOnly(name, capacity, pts);
			
			double totalVar = 0.0;
			double totalVarSqr = 0.0;
			for (int i = 0; i < results.size(); i++) {
				final GvmResult<GvmVectorSpace, Void> result = results.get(i);
				final double var = result.getVariance();
				totalVar += var;
				totalVarSqr += var * var;
			}
			double variance = (totalVar * totalVar - totalVarSqr) / capacity;
			writer.write(String.format("%d %3.3f %3.3f %3.3f%n", capacity, totalVar, totalVar / capacity, variance));
		}
		writer.close();
	}
	
	
	
}
