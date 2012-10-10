package com.tomgibara.geo;

import junit.framework.TestCase;

public class TransformTest extends TestCase {

	private final DatumTransforms transforms = DatumTransforms.getDefaultTransforms();
	
	public void testBasic() {
		testCorrection( GridRefSystem.OSGB36.createGridRef("E651410 N313177") );
		testCorrection( GridRefSystem.OSGB36.createGridRef("NN166712") );
		testCorrection( GridRefSystem.OSI65.createGridRef("J 02598 74444") );
		testCorrection( GridRefSystem.OSI65.createGridRef("H 79972 62472") );
	}
	
	public void testCorrection(GridRef ref) {
		System.out.println(ref.getEasting() +"," + ref.getNorthing());
		LatLon ll = ref.toLatLon();
		System.out.println("Untransformed: " + ll);
		LatLon ll2 = transforms.getTransform(Datum.WSG84).transform(ll.atHeight(0)).getLatLon();
		System.out.println("Transformed: " + ll2);
	}
	
}
