package com.tomgibara.graphics.util;
/*
 * Created on 27-Jun-2005
 */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

/**
 * @author tom
 */

public class GraphicsUtil {

	private static final float fltTwoPi = (float) (Math.PI * 2.0);
	private static final float fltRadToDeg = (float) (180 / Math.PI);
	
	public static Arc2D.Float arcThroughThreePoints(float x1, float y1, float x2, float y2, float x3, float y3, boolean clockwise) {
		float d = 2 * (x1 - x3) * (y3 - y2) + 2 * (x2 - x3) * (y1 - y3);
		float m1 = x1 * x1 - x3 * x3 + y1 * y1 - y3 * y3;
		float m2 = x3 * x3 - x2 * x2 + y3 * y3 - y2 * y2;
		float nx = m1 * (y3 - y2) + m2 * (y3 - y1);
		float ny = m1 * (x2 - x3) + m2 * (x1 - x3);
		float cx = nx / d;
		float cy = ny / d;
		float r = (float) Math.hypot(x1 - cx, y1 - cy);
		float a1 = (float) Math.atan2(-(y1 - cy), x1 - cx);
		float a2 = (float) Math.atan2(-(y3 - cy), x3 - cx);
		float extent = a2 - a1;
		if (!clockwise && extent < 0f) {
			extent = fltTwoPi + extent; 
		} else if (clockwise && extent > 0f) {
			extent = extent - fltTwoPi;
		}
		return new Arc2D.Float(cx - r, cy - r, r * 2, r * 2, a1 * fltRadToDeg, extent  * fltRadToDeg, Arc2D.OPEN);
	}
	
	public static void drawString(Graphics2D g, String string, int x, int y) {
        AffineTransform identity = new AffineTransform();
        g.setTransform(identity);
        g.setColor(Color.BLACK);
        Font font = Font.decode("Arial-BOLD-18");
        GlyphVector vector = font.createGlyphVector(g.getFontRenderContext(), string);
        Shape shape = vector.getOutline();
        g.setStroke(new BasicStroke(3));
        g.setFont(font);
        g.translate(x, y + g.getFontMetrics().getAscent());
        g.draw(shape);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setTransform(identity);
    }

}
