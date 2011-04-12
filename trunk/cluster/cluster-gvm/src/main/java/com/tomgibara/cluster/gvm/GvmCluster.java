package com.tomgibara.cluster.gvm;
import java.util.Arrays;

import com.tomgibara.cluster.NUMBER;

/**
 * A cluster of points.
 * 
 * @author Tom Gibara
 *
 * @param <K> the key type
 */

public class GvmCluster<K> {

	/**
	 * The pairings of this cluster with all other clusters.
	 */
	
	final GvmClusterPair<K>[] pairs;

	/**
	 * Whether this cluster is in the process of being removed.
	 */
	
	boolean removed;
	
	/**
	 * The number of points in this cluster.
	 */
	
	int count;
	
	/**
	 * The total mass of this cluster.
	 */
	
	NUMBER m0;
	
	/**
	 * The mass-weighted coordinate sum.
	 */
	
	final NUMBER[] m1;
	
	/**
	 * The mass-weighted coordinate-square sum.
	 */
	
	final NUMBER[] m2;
	
	/**
	 * The computed variance of this cluster
	 */
	
	double var;
	
	/**
	 * The key associated with this cluster.
	 */
	
	K key;
	
	// constructors
	
	GvmCluster(GvmClusters<?> clusters) {
		removed = false;
		count = 0;
		m0 = NUMBER.zero();
		m1 = new NUMBER[clusters.dimension];
		m2 = new NUMBER[clusters.dimension];
		pairs = new GvmClusterPair[clusters.capacity];
		update();
	}
	
	// public accessors
	
	/**
	 * The total mass of the cluster.
	 */

	public NUMBER getMass() {
		return m0;
	}
	
	/**
	 * The number of points in the cluster.
	 */
	
	public int getCount() {
		return count;
	}
	
	/**
	 * The computed variance of the cluster
	 */

	public double getVariance() {
		return var;
	}
	
	/**
	 * The key associated with the cluster, may be null.
	 */

	public K getKey() {
		return key;
	}

	// package methods

	/**
	 * Completely clears this cluster. All points and their associated mass is
	 * removed along with any key that was assigned to the cluster,
	 */
	
	void clear() {
		count = 0;
		m0 = NUMBER.zero();
		Arrays.fill(m1, NUMBER.zero());
		Arrays.fill(m2, NUMBER.zero());
		var = 0.0;
		key = null;
	}

	/**
	 * Sets this cluster equal to a single point.
	 * 
	 * @param m
	 *            the mass of the point
	 * @param xs
	 *            the coordinates of the point
	 */

	void set(final NUMBER m, final NUMBER[] xs) {
		if (NUMBER.equal(m, NUMBER.zero())) {
			if (count != 0) {
				Arrays.fill(m1, NUMBER.zero());
				Arrays.fill(m2, NUMBER.zero());
			}
		} else {
			for (int i = 0; i < xs.length; i++) {
				final NUMBER x = xs[i];
				m1[i] = NUMBER.product(m, x);
				m2[i] = NUMBER.product(m, NUMBER.square(x));
			}
		}
		count = 1;
		m0 = m;
		var = 0.0;
	}
	
	/**
	 * Adds a point to the cluster.
	 * 
	 * @param m
	 *            the mass of the point
	 * @param xs
	 *            the coordinates of the point
	 */
	
	void add(final NUMBER m, final NUMBER[] xs) {
		if (count == 0) {
			set(m, xs);
		} else {
			count += 1;
			
			if (NUMBER.unequal(m,NUMBER.zero())) {
                //TODO accelerate add
				m0 = NUMBER.sum(m0, m);
				for (int i = 0; i < xs.length; i++) {
					final NUMBER x = xs[i];
                    NUMBER.add(m1, i, NUMBER.product(m, x));
                    NUMBER.add(m2, i, NUMBER.product(m, NUMBER.square(x)));
				}
				update();
			}
		}
	}
	
	/**
	 * Sets this cluster equal to the specified cluster
	 * 
	 * @param cluster a cluster, not this or null
	 */
	
	void set(GvmCluster<K> cluster) {
		if (cluster == this) throw new IllegalArgumentException("cannot set cluster to itself");
		
		m0 = cluster.m0;
		System.arraycopy(cluster.m1, 0, m1, 0, m1.length);
		System.arraycopy(cluster.m2, 0, m2, 0, m2.length);
		var = cluster.var;
	}
	
	/**
	 * Adds the specified cluster to this cluster.
	 * 
	 * @param cluster the cluster to be added
	 */
	
	void add(GvmCluster<K> cluster) {
		if (cluster == this) throw new IllegalArgumentException();
		if (cluster.count == 0) return; //nothing to do
		
		if (count == 0) {
			set(cluster);
		} else {
			count += cluster.count;
			//TODO accelerate add
			m0 = NUMBER.sum(m0, cluster.m0);
			NUMBER[] cm1 = cluster.m1;
            NUMBER[] cm2 = cluster.m2;
			for (int i = 0; i < m1.length; i++) {
				NUMBER.add(m1, i, cm1[i]);
				NUMBER.add(m2, i, cm2[i]);
			}
			
			update();
		}
	}
	
	/**
	 * Computes this clusters variance if it were to have a new point added to it.
	 * 
	 * @param m the mass of the point
	 * @param xs the coordinates of the point
	 * @return the variance of this cluster inclusive of the point
	 */
	
	double test(NUMBER m, final NUMBER[] xs) {
		NUMBER m0 = NUMBER.sum(this.m0, m);
		double var;
		if (NUMBER.equal(m0, NUMBER.zero())) {
			var = 0;
		} else {
			NUMBER sum = NUMBER.zero();
			for (int i = 0; i < m1.length; i++) {
				final NUMBER x = xs[i];
				final NUMBER m1 = NUMBER.sum(this.m1[i], NUMBER.product(m, x));
				final NUMBER m2 = NUMBER.sum(this.m2[i], NUMBER.product(m, NUMBER.square(x)));
				//TODO accelerate add
				sum = NUMBER.sum(sum, NUMBER.difference(NUMBER.product(m2, m0), NUMBER.square(m1)));
			}
			var = GvmClusters.correct(NUMBER.doubleValue(sum)/NUMBER.doubleValue(m0));
		}
		return var - this.var;
	}

	/**
	 * Computes the variance of a cluster that aggregated this cluster with the
	 * supplied cluster.
	 * 
	 * @param cluster
	 *            another cluster
	 * @return the combined variance
	 */

	double test(GvmCluster<K> cluster) {
		final NUMBER m0 = NUMBER.sum(this.m0, cluster.m0);
		if (NUMBER.equal(m0, NUMBER.zero())) {
			return 0.0;
		} else {
			//TODO make symmetric for this
			final NUMBER[] cm1 = cluster.m1;
			final NUMBER[] cm2 = cluster.m2;
			NUMBER sum = NUMBER.zero();
			for (int d = 0; d < m1.length; d++) {
				final NUMBER m1 = NUMBER.sum(this.m1[d], cm1[d]);
				final NUMBER m2 = NUMBER.sum(this.m2[d], cm2[d]);
				//TODO accelerate add
				sum = NUMBER.sum(sum, NUMBER.difference(NUMBER.product(m2, m0), NUMBER.square(m1)));
			}
			return GvmClusters.correct(NUMBER.doubleValue(sum)/NUMBER.doubleValue(m0));
		}
	}

	// private utility methods
	
	/**
	 * Recompute this cluster's variance.
	 */
	
	private void update() {
		if (NUMBER.equal(m0, NUMBER.zero())) {
			var = 0.0;
		} else {
			NUMBER sum = NUMBER.zero();
			for (int i = 0; i < m1.length; i++) {
				//TODO accelerate add
				sum = NUMBER.sum(sum, NUMBER.difference(NUMBER.product(m2[i], m0), NUMBER.square(m1[i])));
			}
			var = GvmClusters.correct(NUMBER.doubleValue(sum)/NUMBER.doubleValue(m0));
		}
	}
	
}
