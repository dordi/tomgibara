package com.tomgibara.graphics.util;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

	private static final Pattern htmlRGB = Pattern.compile("#\\p{XDigit}{6}");
	
	public static Color parseHtmlColor(String str) {
		final Matcher matcher = htmlRGB.matcher(str);
		if (!matcher.matches()) throw new IllegalArgumentException();
		int rgb = Integer.parseInt(str.substring(1), 16);
		return new Color(rgb);
	}
	
}
