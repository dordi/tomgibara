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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class City {

    static void randomize(File in, File out) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in), "UTF-8"));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            lines.add(line);
        }
        reader.close();

        Collections.shuffle(lines);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
        String newline = "\n";
        for (String line : lines) {
            writer.write(line);
            writer.write(newline);
        }
        writer.close();
    }

    static List<City> readCities(InputStream in, int minPop) throws IOException {
        ArrayList<City> cities = new ArrayList<City>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        boolean track = false;
        Writer writer = track ? new OutputStreamWriter(new FileOutputStream("tmpcities.txt"), "UTF-8") : null;
        String line;
        int lineCount = 0;
        int cityCount = 0;
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            lineCount++;

            String[] parts = line.split("\t");
            int pop = Integer.parseInt(parts[14]);
            if (minPop > 0 && pop < minPop) continue;
            if (track) {
            	writer.write(line);
            	writer.write('\n');
            }
            
            City city = new City();
            city.pop = pop;
            city.name = parts[1];
            String[] tmp = parts[17].split("/");
            city.cont = tmp.length == 0 ? "" : tmp[0].intern();
            city.lng = Double.parseDouble(parts[5]);
            city.lat = Double.parseDouble(parts[4]);
            
            cities.add(city);
            cityCount++;
        }
        System.out.println(String.format("%d cities clustered out of possible %,d", cityCount, lineCount));
        reader.close();
        if (track) writer.close();
        return cities;
    }
    
    double lng;
    double lat;
    int pop;
    String name;
    String cont;
}