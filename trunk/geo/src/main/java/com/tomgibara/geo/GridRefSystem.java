package com.tomgibara.geo;

public final class GridRefSystem {

	public static final GridRefSystem OSGB36 = new GridRefSystem(Datum.OSGB36, OSGrid.instance);
	
	public static final GridRefSystem OSI65 = new GridRefSystem(Datum.OSI65, OSIGrid.instance);
	
	private final Datum datum;
	private final Grid grid;

	public GridRefSystem(Datum datum, Grid grid) {
		if (datum == null) throw new IllegalArgumentException("null datum");
		if (grid == null) throw new IllegalArgumentException("null grid");
		this.datum = datum;
		this.grid = grid;
	}
	
	public Datum getDatum() {
		return datum;
	}
	
	public Grid getGrid() {
		return grid;
	}

	public GridRef createGridRef(String str) {
		return grid.refFromString(this, str);
	}
	
	public GridRef createGridRef(int easting, int northing) {
		return new GridRef(this, easting, northing);
	}
	
	@Override
	public int hashCode() {
		return datum.hashCode() ^ grid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof GridRefSystem)) return false;
		GridRefSystem that = (GridRefSystem) obj;
		if (!this.grid.equals(that.grid)) return false;
		if (!this.datum.equals(that.datum)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return datum + " " + grid;
	}

	LatLon gridRefToLatLon(GridRef gridRef) {
		double E = gridRef.getEasting(), N = gridRef.getNorthing();

		Ellipsoid ellipsoid = datum.ellipsoid;
		double N0 = datum.N0, F0 = datum.F0, E0 = datum.E0, lat0 = datum.lat0, lon0 = datum.lon0;
		double a = ellipsoid.a, b = ellipsoid.b, e2 = ellipsoid.e2;
		double n = ellipsoid.n, n2 = ellipsoid.n2, n3 = ellipsoid.n3;
		
		double lat = lat0, M = 0;
		do {
			lat = (N-N0-M)/(a*F0) + lat;
			double Ma = (1 + n + (5.0/4.0)*n2 + (5.0/4.0)*n3) * (lat-lat0);
			double Mb = (3*n + 3*n2 + (21.0/8.0)*n3) * Math.sin(lat-lat0) * Math.cos(lat+lat0);
			double Mc = ((15.0/8.0)*n2 + (15.0/8.0)*n3) * Math.sin(2*(lat-lat0)) * Math.cos(2*(lat+lat0));
			double Md = (35.0/24.0)*n3 * Math.sin(3*(lat-lat0)) * Math.cos(3*(lat+lat0));
			M = b * F0 * (Ma - Mb + Mc - Md);					// meridional arc
		} while (N-N0-M >= 0.00001);							// ie until < 0.01mm
		
		double cosLat = Math.cos(lat), sinLat = Math.sin(lat);
		double nu = a*F0/Math.sqrt(1-e2*sinLat*sinLat);		// transverse radius of curvature
		double rho = a*F0*(1-e2)/Math.pow(1-e2*sinLat*sinLat, 1.5);  // meridional radius of curvature
		double eta2 = nu/rho-1;

		double tanLat = Math.tan(lat);
		double tan2lat = tanLat*tanLat, tan4lat = tan2lat*tan2lat, tan6lat = tan4lat*tan2lat;
		double secLat = 1/cosLat;
		double nu3 = nu*nu*nu, nu5 = nu3*nu*nu, nu7 = nu5*nu*nu;
		double VII = tanLat/(2*rho*nu);
		double VIII = tanLat/(24*rho*nu3)*(5+3*tan2lat+eta2-9*tan2lat*eta2);
		double IX = tanLat/(720*rho*nu5)*(61+90*tan2lat+45*tan4lat);
		double X = secLat/nu;
		double XI = secLat/(6*nu3)*(nu/rho+2*tan2lat);
		double XII = secLat/(120*nu5)*(5+28*tan2lat+24*tan4lat);
		double XIIA = secLat/(5040*nu7)*(61+662*tan2lat+1320*tan4lat+720*tan6lat);

		double dE = (E-E0), dE2 = dE*dE, dE3 = dE2*dE, dE4 = dE2*dE2, dE5 = dE3*dE2, dE6 = dE4*dE2, dE7 = dE5*dE2;
		lat = lat - VII*dE2 + VIII*dE4 - IX*dE6;
		double lon = lon0 + X*dE - XI*dE3 + XII*dE5 - XIIA*dE7;

		return new LatLon(datum, Math.toDegrees(lat), Math.toDegrees(lon));
	}
}
