package com.tomgibara.cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.tomgibara.cluster.gvm.dbl.DblClusters;
import com.tomgibara.cluster.gvm.dbl.DblListKeyer;
import com.tomgibara.cluster.gvm.dbl.DblResult;

public class ClusterPoints {

	public static void main(String[] args) throws IOException {
		cluster("cross", 2);
		cluster("gmouse", 3);
		cluster("umouse", 3);
		cluster("faithful", 2);
	}

	private static void cluster(String name, int capacity) throws IOException {
		Scanner scanner = new Scanner(new File("../cluster-common/R/" + name + ".txt"));
		ArrayList<double[]> pts = new ArrayList<double[]>();
		while (scanner.hasNext()) {
			double[] pt = new double[2];
			pt[0] = scanner.nextDouble();
			pt[1] = scanner.nextDouble();
			pts.add(pt);
		}
		scanner.close();
		
		Collections.shuffle(pts, new Random(0L));
		
		DblClusters<List<double[]>> clusters = new DblClusters<List<double[]>>(2, capacity);
		clusters.setKeyer(new DblListKeyer<double[]>());
		for (double[] pt : pts) {
			ArrayList<double[]> key = new ArrayList<double[]>();
			key.add(pt);
			clusters.add(1.0, pt, key);
		}
		
		FileWriter writer = new FileWriter("../cluster-common/R/" + name + "-clustered.txt");
		final List<DblResult<List<double[]>>> results = clusters.results();
		for (int i = 0; i < results.size(); i++) {
			for (double[] pt : results.get(i).getKey()) {
				writer.write(String.format("%3.3f %3.3f %d%n", pt[0], pt[1], i+1));
			}
		}
		writer.close();
	}

}
