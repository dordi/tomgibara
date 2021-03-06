package com.tomgibara.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterPainter<C,P> {

	public interface Sizer<C> {

		double distance(C c1, C c2);
		
		double radius(C c);
		
		long points(C c);
		
	}
	
	private final Sizer<C> sizer;
	private final List<P> paints;
	
	public ClusterPainter(Sizer<C> sizer, List<P> paints) {
		if (sizer == null) throw new IllegalArgumentException("null sizer");
		if (paints == null) throw new IllegalArgumentException("null paints");
		this.sizer = sizer;
		this.paints = new ArrayList<P>(paints);
	}

	public Map<C, P> paint(Collection<? extends C> cs) {
		C[] clusters = (C[]) cs.toArray();
		int length = clusters.length;
		
		// sort clusters by radius - biggest first
		Arrays.sort(clusters, new ClusterComparator());
		
		// build table of radii
		double[] radii = new double[length];
		for (int i = 0; i < length; i++) {
			radii[i] = sizer.radius(clusters[i]);
		}
		
		// build table of distances
		final Distance NULL = new Distance();
		Distance[][] distances = new Distance[length][length];
		for (int i = 0; i < length; i++) {
			C c1 = clusters[i];
			for (int j = 0; j <= i; j++) {
				if (i == j) {
					distances[i][j] = NULL;
				} else {
					C c2 = clusters[j];
					double d = sizer.distance(c1, c2) - radii[i] - radii[j];
					Distance distance = new Distance(c1, c2, d);
					distances[i][j] = distance;
					distances[j][i] = distance;
				}
			}
		}
		
		// order distances - shortest first
		for (int i = 0; i < length; i++) {
			Arrays.sort(distances[i]);
		}

		//paint clusters
		int count = paints.size();
		BitSet used = new BitSet(count);
		Map<C, P> map = new HashMap<C, P>();
		outer: for (int i = 0; i < length; i++) {
			C c1 = clusters[i];
			Distance[] ds = distances[i];
			used.clear();
			for (int j = 0; j < length; j++) {
				Distance d = ds[j];
				C c2 = d.other(c1);
				if (c2 == null) continue;
				P p = map.get(c2);
				if (p != null) {
					//TODO yuk!
					int k = paints.indexOf(p);
					used.set(k);
					if (used.cardinality() == count) {
						map.put(c1, p);
						continue outer;
					}
				}
			}
			for (int j = 0; j < count; j++) {
				if (!used.get(j)) {
					map.put(c1, paints.get(j));
					continue outer;
				}
			}
		}
		
		// return result
		return map;
	}
	
	private class ClusterComparator implements Comparator<C> {
		
		public int compare(C c1, C c2) {
			if (c1 == c2) return 0;
			long p1 = sizer.points(c1); 
			long p2 = sizer.points(c2);
			if (p1 == p2) return 0;
			return p1 < p2 ? 1 : 0;
		}
		
	}
	
	private static class Distance implements Comparable<Distance> {
		
		final Object c1;
		final Object c2;
		final double d;
		
		Distance() {
			c1 = null;
			c2 = null;
			d = 0.0;
		}
		
		Distance(Object c1, Object c2, double d) {
			this.c1 = c1;
			this.c2 = c2;
			this.d = d;
		}
		
		@SuppressWarnings("unchecked")
		<C> C other(C c) {
			if (c == c1) return (C) c2;
			if (c == c2) return (C) c1;
			return null;
		}
		
		@Override
		public int compareTo(Distance that) {
			if (this == that || this.d == that.d) return 0;
			return this.d < that.d ? -1 : 1;
		}
		
	}
	
}
