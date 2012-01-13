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

import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFrame;

public class CityApp {

	private enum Source { ORIG, RAND, NEW_RAND, RES };
	
	private static final int CITY_MIN_POP = 100000;

    public static void main(String[] args) throws IOException {
        Source source = Source.RES;
        File original = new File("files/cities1000.txt");
        File shuffled = new File("files/cities-random.txt");
        final InputStream in;
        if (source == Source.NEW_RAND) {
            City.randomize(original, shuffled);
        }
        if (source == Source.RES) {
        	in = CityApp.class.getClassLoader().getResourceAsStream("cities.txt");
        } else if (source == Source.ORIG) {
            in = new FileInputStream(original);
        } else {
            in = new FileInputStream(shuffled);
        }
        
        List<City> cities = City.readCities(in, CITY_MIN_POP);

        JFrame frame = new JFrame("Clustering Demonstration - World Cities");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        CityDemo.insert(pane, cities, new Dimension(800, 400), true);
        frame.pack();
        frame.setVisible(true);
    }

}
