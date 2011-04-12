package com.tomgibara.cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.math.random.GaussianRandomGenerator;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.NormalizedRandomGenerator;
import org.apache.commons.math.random.UncorrelatedRandomVectorGenerator;

public class CreateGaussianMouse {

	public static void main(String[] args) throws IOException {
		GaussianRandomGenerator gen = new GaussianRandomGenerator(new JDKRandomGenerator());
		FileWriter writer = new FileWriter("R/gmouse.txt");
		try {
			writeCluster(gen, new double[] {0,0}, new double[] {4, 4}, 100, writer);
			writeCluster(gen,new double[] {-4,4}, new double[] {2, 2}, 50, writer);
			writeCluster(gen,new double[] {4,4}, new double[] {2, 2}, 50, writer);
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
