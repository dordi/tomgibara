package com.tomgibara.geo;

public final class GridRef {

	private final GridRefSystem system;
	private final int easting;
	private final int northing;

	GridRef(GridRefSystem system, int easting, int northing) {
		if (system == null) throw new IllegalArgumentException("null grid");
		this.system = system;
		this.easting = easting;
		this.northing = northing;
	}

	public GridRefSystem getSystem() {
		return system;
	}
	
	public int getEasting() {
		return easting;
	}
	
	public int getNorthing() {
		return northing;
	}
	
	public LatLon toLatLon() {
		return system.gridRefToLatLon(this);
	}
	
	@Override
	public int hashCode() {
		return system.hashCode() ^ easting ^ 31 * northing;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof GridRef)) return false;
		GridRef that = (GridRef) obj;
		if (this.easting != that.easting) return false;
		if (this.northing != that.northing) return false;
		if (!this.system.equals(that.system)) return false;
		return true;
	}

	@Override
	public String toString() {
		return system.getGrid().refToString(this);
	}
	
}
