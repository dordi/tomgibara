package com.tomgibara.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.tomgibara.cluster.gvm.GvmClusters;
import com.tomgibara.cluster.gvm.GvmListKeyer;
import com.tomgibara.cluster.gvm.GvmNullKeyer;
import com.tomgibara.cluster.gvm.GvmResult;
import com.tomgibara.cluster.gvm.space.GvmVectorSpace;

public class ClusterFiles {

	public static final GvmVectorSpace space = new GvmVectorSpace(2);
	
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
	
	public static List<GvmVectorSpace.Vector> read(String name) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("../cluster-common/R/" + name + ".txt"));
		ArrayList<GvmVectorSpace.Vector> pts = new ArrayList<GvmVectorSpace.Vector>();
		while (scanner.hasNext()) {
			pts.add( space.newVector(new double[] {scanner.nextDouble(), scanner.nextDouble()}) );
		}
		scanner.close();
		return pts;
	}
	
	public static List<GvmResult<GvmVectorSpace.Vector, List<GvmVectorSpace.Vector>>> cluster(String name, int capacity) throws IOException {
		List<GvmVectorSpace.Vector> pts = ClusterFiles.read(name);
		Collections.shuffle(pts, new Random(0L));
		
		long start = System.currentTimeMillis();
		GvmClusters<GvmVectorSpace, GvmVectorSpace.Vector, List<GvmVectorSpace.Vector>> clusters = new GvmClusters<GvmVectorSpace, GvmVectorSpace.Vector, List<GvmVectorSpace.Vector>>(space, capacity);
		clusters.setKeyer(new GvmListKeyer<GvmVectorSpace.Vector, GvmVectorSpace.Vector>());
		for (GvmVectorSpace.Vector pt : pts) {
			ArrayList<GvmVectorSpace.Vector> key = new ArrayList<GvmVectorSpace.Vector>();
			key.add(pt);
			clusters.add(1.0, pt, key);
		}
		long finish = System.currentTimeMillis();
		System.out.println(name + " (" + capacity + ") " + (finish-start) + " ms");
		return clusters.results();
	}

	public static List<GvmResult<GvmVectorSpace.Vector, Void>> clusterOnly(String name, int capacity, List<GvmVectorSpace.Vector> pts) throws IOException {
		if (pts == null) {
			pts = ClusterFiles.read(name);
			Collections.shuffle(pts, new Random(0L));
		}
		
		long start = System.currentTimeMillis();
		GvmClusters<GvmVectorSpace, GvmVectorSpace.Vector, Void> clusters = new GvmClusters<GvmVectorSpace, GvmVectorSpace.Vector, Void>(space, capacity);
		clusters.setKeyer(new GvmNullKeyer<GvmVectorSpace.Vector, Void>());
		for (GvmVectorSpace.Vector pt : pts) {
			clusters.add(1.0, pt, null);
		}
		long finish = System.currentTimeMillis();
		System.out.println(name + " (" + capacity + ") " + (finish-start) + " ms");
		return clusters.results();
	}

}
