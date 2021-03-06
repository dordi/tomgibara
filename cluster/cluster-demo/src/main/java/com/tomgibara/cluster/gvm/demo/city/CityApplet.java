/*
 * Copyright 2007 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.cluster.gvm.demo.city;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JApplet;

public class CityApplet extends JApplet {

	private static final long serialVersionUID = 1077860670861265613L;

	@Override
	public void init() {
		InputStream in = null;
		try {
			in = CityApp.class.getClassLoader().getResourceAsStream("cities.txt");
	        List<City> cities = City.readCities(in, 0);
	        CityDemo.insert(this, cities, null, false);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					/* ignore */
				}
			}
		}
	}
	
}
