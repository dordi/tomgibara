package com.tomgibara.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.tomgibara.cluster.gvm.dbl.DblClusters;
import com.tomgibara.cluster.gvm.dbl.DblListKeyer;
import com.tomgibara.cluster.gvm.dbl.DblNullKeyer;
import com.tomgibara.cluster.gvm.dbl.DblResult;

public class ClusterFiles {

	public static final Map<String, Integer> files;
	
	static {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		map.put("cross", 2);
		map.put("gmouse", 3);
		map.put("umouse", 3);
		map.put("faithful", 2);
		map.put("circles", 81);
		files = Collections.unmodifiableMap(map);
	}
	
	public static List<double[]> read(String name) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("../cluster-common/R/" + name + ".txt"));
		ArrayList<double[]> pts = new ArrayList<double[]>();
		while (scanner.hasNext()) {
			double[] pt = new double[2];
			pt[0] = scanner.nextDouble();
			pt[1] = scanner.nextDouble();
			pts.add(pt);
		}
		scanner.close();
		return pts;
	}
	
	public static List<DblResult<List<double[]>>> cluster(String name, int capacity) throws IOException {
		List<double[]> pts = ClusterFiles.read(name);
		Collections.shuffle(pts, new Random(0L));
		
		long start = System.currentTimeMillis();
		DblClusters<List<double[]>> clusters = new DblClusters<List<double[]>>(2, capacity);
		clusters.setKeyer(new DblListKeyer<double[]>());
		for (double[] pt : pts) {
			ArrayList<double[]> key = new ArrayList<double[]>();
			key.add(pt);
			clusters.add(1.0, pt, key);
		}
		long finish = System.currentTimeMillis();
		System.out.println(name + " (" + capacity + ") " + (finish-start) + " ms");
		return clusters.results();
	}

	public static List<DblResult<Void>> clusterOnly(String name, int capacity, List<double[]> pts) throws IOException {
		if (pts == null) {
			pts = ClusterFiles.read(name);
			Collections.shuffle(pts, new Random(0L));
		}
		
		long start = System.currentTimeMillis();
		DblClusters<Void> clusters = new DblClusters<Void>(2, capacity);
		clusters.setKeyer(new DblNullKeyer<Void>());
		for (double[] pt : pts) {
			clusters.add(1.0, pt, null);
		}
		long finish = System.currentTimeMillis();
		System.out.println(name + " (" + capacity + ") " + (finish-start) + " ms");
		return clusters.results();
	}

}
