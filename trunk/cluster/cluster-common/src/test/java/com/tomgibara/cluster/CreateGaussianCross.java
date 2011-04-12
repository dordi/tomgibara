package com.tomgibara.cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.math.random.GaussianRandomGenerator;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.NormalizedRandomGenerator;
import org.apache.commons.math.random.UncorrelatedRandomVectorGenerator;

public class CreateGaussianCross {

	public static void main(String[] args) throws IOException {
		GaussianRandomGenerator gen = new GaussianRandomGenerator(new JDKRandomGenerator());
		final double[] center = new double[] {0,0};
		int clusterSize = 300;
		FileWriter writer = new FileWriter("R/cross.txt");
		try {
			writeCluster(gen, center, new double[] {6, 1}, clusterSize, writer);
			writeCluster(gen, center, new double[] {1, 6}, clusterSize, writer);
		} finally {
			writer.close();
		}
	}

	private static void writeCluster(NormalizedRandomGenerator gen, double[] means, double[] deviations, int size, Writer writer) throws IOException {
		UncorrelatedRandomVectorGenerator c = new UncorrelatedRandomVectorGenerator(means, deviations, gen);
		for (int i = 0; i < size; i++) {
			double[] pt = c.nextVector();
			writer.write(String.format("%3.3f %3.3f%n", pt[0], pt[1]));
		}
	}
	
}
