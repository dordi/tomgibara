package com.tomgibara.geo;

public class GeoSample {

	public static void main(String[] args) {
		
		/*
		 * Convert an Ordnance Survey grid reference...
		 */
		
		GridRef ref = GridRefSystem.OSGB36.createGridRef("TQ 270 775");
		
		/*
		 * ... into eastings and northings...
		 */
		
		ref.getEasting();
		ref.getNorthing();

		/*
		 * ... or a latitude and longitude
		 * (which remains relative to the Ordnance Survey reference).
		 */
		
		ref.toLatLon();

		/*
		 * If you want to change the coordinates to use a different standard,
		 * say to WGS84 (used by GPS), you need to obtain a transform.
		 */
		
		DatumTransform transform = DatumTransforms.getDefaultTransforms().getTransform(Datum.WSG84);
		
		/*
		 * The transform operates on points in 3D space,
		 * so you need to be explict about the height when you apply the transform.
		 */
		
		LatLonHeight coords = transform.transform(ref.toLatLon().atHeight(0));
		
		/*
		 * The resulting coordinates also include a height,
		 * but the underlying latitude and longitude is easily recovered.
		 */
		
		coords.getLatLon();
		
		/*
		 * If you need to support other reference systems, you can define your own ellipsoids...
		 */
		
		Ellipsoid ED50 = Ellipsoid.withAxes(6378388.000, 6356911.946);
		
		/*
		 * ... datums ...
		 */
		
		Datum UTM_32N = Datum.withDegreesMeters(ED50, 0.9996, 0, 9, 500000, 0);
		
		/*
		 * ... and grids.
		 */
		
		Grid grid = new Grid() { /*...*/ };
		
		/*
		 * Which can be combined into a new reference system.
		 */
		
		GridRefSystem ELD79 = GridRefSystem.withDatumAndGrid(UTM_32N, grid);
	}
	
	private static class Grid implements com.tomgibara.geo.Grid {

		@Override
		public GridRef refFromString(GridRefSystem system, String str) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String refToString(GridRef ref) {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
