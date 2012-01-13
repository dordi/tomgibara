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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

class WorldMap extends JComponent {

    private static final Color DEFAULT_PIN_COLOR = Color.GRAY;
    private static final int DEFAULT_PIN_RADIUS = 10;
    private static final int PIN_SCALE = 800;
    
    private static final BufferedImage image;
    
    static {
        BufferedImage tmp;
        try {
        	InputStream in = WorldMap.class.getClassLoader().getResourceAsStream("world.jpg");
        	if (in == null) in = new FileInputStream(new File("images/world.jpg"));
            tmp = ImageIO.read(in);
        } catch (IOException e) {
            tmp = null;
        }
        image = tmp;
    }
    
    private static final String fontName = "Lucida Sans"; //Font.SANS_SERIF
    private static final Font font = new Font(fontName, 1, Font.PLAIN);
    
    private TreeSet<Pin> pins;
    private String title;
    private String caption;
    
    WorldMap() {
        pins = new TreeSet<Pin>(new PinComparator());
    }
    
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d = getSize();
        g.setColor(new Color(20, 10, 90));
        g.fillRect(0, 0, d.width, d.height);
        int dx = 0;
        int dy = 0;
        int dw = d.width;
        int dh = d.height;
        if (d.width > 2 * d.height) {
        	dx = (d.width - d.height * 2) / 2;
        	d.width = d.height * 2;
        	dw = 2 * dh;
        } else if (d.width < 2 * d.height) {
        	dy = (d.height - d.width / 2) / 2;
        	d.height = d.width / 2;
        	dh = dw / 2;
        }
        g.translate(dx, dy);
        if (image != null) g.drawImage(image, 0, 0, dw, dh, null);
        synchronized (pins) {
            for (Pin pin : pins) {
                drawPin(g, d, pin);
            }
            for (Pin pin : pins) {
                drawLabel(g, d, pin);
            }
        }
        g.translate(-dx, -dy);
        drawString(g, title, true);
        drawString(g, caption, false);
    }

    private Color mix(Color c1, Color c2) {
        return new Color((c1.getRed()+c2.getRed())/2, (c1.getGreen()+c2.getGreen())/2, (c1.getBlue()+c2.getBlue())/2);
    }
    
    private void drawPin(Graphics2D g, Dimension d, Pin pin) {
    	if (!pin.isVisible()) return;
    	
		int x = (int) (pin.getX() * d.width);
		int y = (int) (pin.getY() * d.height);
        int radius = pin.getRadius() < 1 ? DEFAULT_PIN_RADIUS : pin.getRadius();
        radius = radius * d.width / PIN_SCALE;
        Color color = pin.getColor() == null ? DEFAULT_PIN_COLOR : pin.getColor();
        int b = Math.min(radius/2, 2);
        Pin child = pin.getChild();

        if (child != null) {
        	int cx = (int) (child.getX() * d.width);
        	int cy = (int) (child.getY() * d.height);
        	int cr = child.getRadius() < 1 ? DEFAULT_PIN_RADIUS : child.getRadius();
            cr = cr * d.width / PIN_SCALE;
            int w = Math.min(cr, radius);
            if (w < 0) w = 1;

            g.setColor(color);
        	g.setStroke(new BasicStroke(w));
        	g.drawLine(x, y, cx, cy);
        }
        
        if (b > 0) {
	        g.setColor(mix(color, Color.BLACK));
	        g.fillOval(x - radius + b, y - radius + b, radius * 2, radius * 2);
	        g.setColor(mix(color, Color.WHITE));
	        g.fillOval(x - radius - b, y - radius - b, radius * 2, radius * 2);
	        g.setColor(mix(color, Color.GRAY));
	        g.fillOval(x - radius - b, y - radius + b, radius * 2, radius * 2);
	        g.setColor(mix(color, Color.GRAY));
	        g.fillOval(x - radius + b, y - radius - b, radius * 2, radius * 2);
        }
        g.setColor(color);
        g.fillOval(x - radius - b/2, y - radius - b/2, radius * 2 + b, radius * 2 + b);

        if (child != null) drawPin(g, d, child);
    }

    private void drawLabel(Graphics2D g, Dimension d, Pin pin) {
    	if (!pin.isVisible()) return;
    	
    	if (pin.getLabel() != null) {
	        int radius = pin.getRadius() < 1 ? DEFAULT_PIN_RADIUS : pin.getRadius();
	        radius = radius * d.width / PIN_SCALE;
	        Color color = pin.getColor() == null ? DEFAULT_PIN_COLOR : pin.getColor();
	
	        Color labelColor = Color.WHITE;
	        Font f = font.deriveFont(radius * 1.6f * 0.75f);
	        g.setFont(f);
	        GlyphVector glyphs = f.createGlyphVector(g.getFontRenderContext(), pin.getLabel());
	        Shape shape = glyphs.getOutline();
	        Rectangle bounds = shape.getBounds();
			int x = (int) (pin.getX() * d.width);
			int y = (int) (pin.getY() * d.height);
	        int lx = x - (int) Math.round( bounds.getWidth() / 2.0 );
	        int ly = y + (int) Math.round( bounds.getHeight() / 2.0 );
	        g.translate(lx, ly);
	        g.setColor(mix(color, Color.BLACK));
	        g.setStroke(new BasicStroke(2f));
	        g.draw(shape);
	        g.setColor(labelColor);
	        g.fill(shape);
	        g.translate(-lx, -ly);
    	}

        Pin child = pin.getChild();
        if (child != null) drawLabel(g, d, child);
    }

    void setCaption(String caption) {
        this.caption = caption;
    }
    
    void setTitle(String title) {
        this.title = title;
    }
    
    boolean addPin(Pin pin) {
        synchronized (pins) {
            boolean ret = pins.add(pin);
            return ret;
        }
    }
    
    boolean addAllPins(Collection<Pin> pins) {
        synchronized (pins) {
            boolean ret = this.pins.addAll(pins);
            return ret;
        }
    }
    
    boolean clearPins() {
        synchronized (pins) {
            boolean ret = pins.size() > 0;
            if (ret) {
                pins.clear();
            }
            return ret;
        }
    }

    private void drawString(Graphics2D g, String str, boolean tr) {
        if (str == null || str.length() == 0) return;

        float size = tr ? 30f : 18f;
        Font f = font.deriveFont(size);
        if (tr) f = f.deriveFont(Font.ITALIC);
        GlyphVector glyphs = f.createGlyphVector(g.getFontRenderContext(), str);
        Shape shape = glyphs.getOutline();
        Rectangle2D bounds2D = shape.getBounds2D();

        int x;
        int y;
        if (tr) {
            x = 10;
            y = 10 - (int) bounds2D.getMinY();
        } else {
            x = getWidth() - 10 - (int) bounds2D.getWidth();
            y = getHeight() - 10;
        }
        g.translate(x, y);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.draw(shape);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.translate(-x, -y);
        
    }
    
}