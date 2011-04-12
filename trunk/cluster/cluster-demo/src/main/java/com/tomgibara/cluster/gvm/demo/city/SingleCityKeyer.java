package com.tomgibara.cluster.gvm.demo.city;

import com.tomgibara.cluster.gvm.dbl.DblSimpleKeyer;

public class SingleCityKeyer extends DblSimpleKeyer<City> {

	@Override
	protected City combineKeys(City city1, City city2) {
		return city1.pop < city2.pop ? city2 : city1;
	}
	
}
