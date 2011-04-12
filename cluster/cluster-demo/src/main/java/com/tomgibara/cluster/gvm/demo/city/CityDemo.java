package com.tomgibara.cluster.gvm.demo.city;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tomgibara.cluster.gvm.dbl.DblListKeyer;
import com.tomgibara.cluster.gvm.dbl.DblClusters;
import com.tomgibara.cluster.gvm.dbl.DblResult;

/*
 * Created on 06-Aug-2007
 */

public class CityDemo {

    private static final int CITY_NAME_MAX = 20;
    
    private static final Map<String, Color> CONTINENT_COLORS = new HashMap<String, Color>();
    
    static {
        CONTINENT_COLORS.put("Africa", new Color(128, 0, 0));
        CONTINENT_COLORS.put("Europe", new Color(0, 180, 0));
        CONTINENT_COLORS.put("America", new Color(255, 0, 0));
        CONTINENT_COLORS.put("Asia", new Color(180, 128, 0));
        CONTINENT_COLORS.put("Australia", new Color(0, 180, 255));
    }
    
    public static void insert(final Container c, final List<City> cities, Dimension d) {
        c.setLayout(new BorderLayout());
        final WorldMap map = new WorldMap();
        if (d != null) map.setPreferredSize(d);
        c.add(map, BorderLayout.CENTER);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox checkbox = new JCheckBox("Display Cities", false);
        panel.add(checkbox);
        c.add(panel, BorderLayout.NORTH);
        JSlider slider = new JSlider(1, 200);
        slider.setValue(10);
        Dictionary<Integer, Component> labels = new Hashtable<Integer, Component>();
        labels.put(1, new JLabel("1"));
        labels.put(50, new JLabel("50"));
        labels.put(100, new JLabel("100"));
        labels.put(150, new JLabel("150"));
        labels.put(200, new JLabel("200"));
        slider.setLabelTable(labels);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        c.add(slider, BorderLayout.SOUTH);

        List<DblResult<City>> results = clusterCities(cities, slider.getValue());
        List<Pin> pins = pinsFromResults(results);
        map.addAllPins(pins);
        //TODO tidy up
        map.repaint();
        map.setTitle(String.format("%,3d largest cities...", cities.size()));
        
        final AtomicInteger currentValue = new AtomicInteger( 0 );
        final AtomicInteger desiredValue = new AtomicInteger( slider.getValue() );
        final AtomicBoolean currentDisplay = new AtomicBoolean(true);
        final AtomicBoolean desiredDisplay = new AtomicBoolean(true);

        new UpdateMap(map, cities, currentDisplay, desiredDisplay, currentValue, desiredValue).run();
        
        final Timer timer = new Timer(true);
        
        checkbox.addChangeListener(new ChangeListener() {
        	@Override
        	public void stateChanged(ChangeEvent e) {
        		JCheckBox checbox = (JCheckBox) e.getSource();
        		desiredDisplay.set(!checbox.isSelected());
                UpdateMap update = new UpdateMap(map, cities, currentDisplay, desiredDisplay, currentValue, desiredValue);
                timer.schedule(update, new Date());
        	}
        });
        
        slider.addChangeListener(new ChangeListener() {
        	@Override
            public void stateChanged(ChangeEvent e) {
        		JSlider slider = (JSlider) e.getSource();
            	desiredValue.set(slider.getValue());
                UpdateMap update = new UpdateMap(map, cities, currentDisplay, desiredDisplay, currentValue, desiredValue);
                timer.schedule(update, new Date());
            } 
        });
    }

    // city methods

    private static List<DblResult<List<City>>> clusterCities2(Collection<City> cities, int maxClusters) {
        double[] xs = new double[2];
        DblClusters<List<City>> clusters = new DblClusters<List<City>>(2, maxClusters);
        clusters.setKeyer(new DblListKeyer<City>());
        for (City city : cities) {
            xs[0] = city.lng;
            xs[1] = city.lat;
            double m = city.pop;
            ArrayList<City> list = new ArrayList<City>();
            list.add(city);
            clusters.add(m, xs, list);
        }
        List<DblResult<List<City>>> results = clusters.results();
        return results;
    }

    private static List<Pin> pinsFromResults2(List<DblResult<List<City>>> results) {
        ArrayList<Pin> pins = new ArrayList<Pin>();
        int count = 0;
        for (DblResult<List<City>> result : results) {
            double lng = result.getCoords()[0];
            double lat = result.getCoords()[1];
        	Color color = new Color((int) ((lng + 180.0) * 256.0 / 360.0), (int) ((lat + 90.0) * 256.0 / 180.0), count * 256 / results.size());
        	List<City> cities = result.getKey();
        	for (City city : cities) {
        		Pin pin = pinFromCity2(city, color);
                pins.add(pin);
			}
        	count++;
		}
        return pins;
    }

    private static Pin pinFromCity2(City city, Color color) {
        double lng = city.lng;
        double lat = city.lat;
        int pop = city.pop;
        
        int radius = radiusFromPop2(pop);
        float x = (float) ((180.0 + lng) / 360.0);
        float y = (float) ((90.0 - lat) / 180.0);
        
        Pin pin = new Pin();
        pin.setX(x);
        pin.setY(y);
        pin.setRadius(radius); 
        pin.setZ(-radius);
        pin.setColor(color);
        return pin;
    }


    private static int radiusFromPop2(double pop) {
        double poplog = Math.log(pop);
        return 1 + (int) (poplog*poplog*poplog/900.0);
    }
    
    //cluster methods
    
    private static List<DblResult<City>> clusterCities(Collection<City> cities, int maxClusters) {
        DblClusters<City> clusters = new DblClusters<City>(2, maxClusters);
        clusters.setKeyer(new SingleCityKeyer());
        double[] xs = new double[2];
        for (City city : cities) {
            xs[0] = city.lng;
            xs[1] = city.lat;
            double m = city.pop;
            clusters.add(m, xs, city);
        }
        
        List<DblResult<City>> results = clusters.results();
        return results;
    }
    
    private static List<Pin> pinsFromResults(List<DblResult<City>> results) {
        ArrayList<Pin> pins = new ArrayList<Pin>();
        Collections.sort(results, new ResultComp());
        int pinCount = 0;
        int hiddenLimit = results.size() - 20;
        for (DblResult<City> result : results) {
            Pin pin = pinFromResult(result);
            if (pinCount >= hiddenLimit) pin.setChild(pinFromCity(result.getKey()));
            pins.add(pin);
            pinCount ++;
		}
        return pins;
    }

    private static Pin pinFromCity(City city) {
        String continent = city.cont;
        String name = city.name;
        double lng = city.lng;
        double lat = city.lat;
        int pop = city.pop;
        
        String label = name.length() > CITY_NAME_MAX ? name.substring(0, CITY_NAME_MAX - 3) + "..." : name;
        Color color = CONTINENT_COLORS.get(continent);
        int radius = radiusFromPop(pop);
        float x = (float) ((180.0 + lng) / 360.0);
        float y = (float) ((90.0 - lat) / 180.0);
        
        Pin pin = new Pin();
        pin.setX(x);
        pin.setY(y);
        pin.setLabel(label);
        pin.setRadius(radius); 
        pin.setZ(-radius);
        pin.setColor(color);
        return pin;
    }

    private static Pin pinFromResult(DblResult<City> result) {
        String continent = result.getKey().cont;
        double lng = result.getCoords()[0];
        double lat = result.getCoords()[1];
        double pop = result.getMass();

        String label = null;
        Color color = CONTINENT_COLORS.get(continent);
        int radius = (int) Math.sqrt( result.getVariance() / pop );
        float x = (float) ((180.0 + lng) / 360.0);
        float y = (float) ((90.0 - lat) / 180.0);

        Pin pin = new Pin();
        pin.setX(x);
        pin.setY(y);
        pin.setLabel(label);
        pin.setRadius(radius); 
        pin.setZ(-radius);
        pin.setColor(color);
        return pin;
    }

    private static int radiusFromPop(double pop) {
        double poplog = Math.log(pop);
        return 3 + (int) (poplog*poplog*poplog/600.0);
    }
    
    private static class ResultComp implements Comparator<DblResult<City>> {
        public int compare(DblResult<City> r1, DblResult<City> r2) {
            double m1 = r1.getMass();
            double m2 = r2.getMass();
            if (m1 == m2) return 0;
            return m1 < m2 ? -1 : 1;
        }
    }
    
    private static class UpdateMap extends TimerTask {

    	private WorldMap map;
        private List<City> cities;
		private AtomicBoolean currentDisplay;
		private AtomicBoolean desiredDisplay;
		private AtomicInteger desiredValue;
		private AtomicInteger currentValue;

		public UpdateMap(WorldMap map, List<City> cities, AtomicBoolean currentDisplay, AtomicBoolean desiredDisplay, AtomicInteger currentValue, AtomicInteger desiredValue) {
        	this.map = map;
        	this.cities = cities;
        	this.currentDisplay = currentDisplay;
        	this.desiredDisplay = desiredDisplay;
        	this.desiredValue = desiredValue;
        	this.currentValue = currentValue;
		}

		public void run() {
            int newValue = desiredValue.get();
            int oldValue = currentValue.getAndSet(newValue);
            boolean newDisplay = desiredDisplay.get();
            boolean oldDisplay = currentDisplay.getAndSet(newDisplay);
            if (oldDisplay == newDisplay &&  oldValue == newValue) return;
            if (desiredDisplay.get()) {
            	doClusterUpdate(newValue);
            } else {
            	doCityUpdate(newValue);
            }
        };

		private void doCityUpdate(int count) {
	    	long start = System.currentTimeMillis();
	        List<DblResult<List<City>>> results = clusterCities2(cities, count);
	        long finish = System.currentTimeMillis();
	        List<Pin> pins = pinsFromResults2(results);
	        map.clearPins();
	        map.addAllPins(pins);
	        map.setCaption(String.format("...into %d clusters (%,3d ms)", results.size(), finish - start));
	        //TODO tidy up
	        map.repaint();
		}

		private void doClusterUpdate(int count) {
	    	long start = System.currentTimeMillis();
	        List<DblResult<City>> results = clusterCities(cities, count);
	        long finish = System.currentTimeMillis();
	        List<Pin> pins = pinsFromResults(results);
	        map.clearPins();
	        map.addAllPins(pins);
	        map.setCaption(String.format("...into %d clusters (%,3d ms)", results.size(), finish - start));
	        //TODO tidy up
	        map.repaint();
        }
        
    }
    
}
