package com.tomgibara.graphics.util;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ImageUtil {

	//work around for bug 4886732
	//public static final boolean reverseComponents = System.getProperty("os.name").startsWith("Windows");
	public static final boolean reverseComponents = true;
	
    public enum IntensityModel {
        AVERAGE, RED, GREEN, BLUE;
        
        byte combine(int r, int g, int b) {
            switch (this) {
            case AVERAGE: return (byte) ((r + b + g) / 3);
            case RED: return (byte) r;
            case GREEN: return (byte) g;
            case BLUE: return (byte) b;
            default: throw new IllegalStateException();
            }
        }

        byte combine(byte r, byte g, byte b) {
            return combine( r & 0xff, g & 0xff, b & 0xff);
        }
    }

    public static byte[] toByteIntensity(BufferedImage image, int type, IntensityModel model, byte[] bytes) {
        if (model == null) model = IntensityModel.AVERAGE;
        int width = image.getWidth();
        int height = image.getHeight();
        switch(type) {
        case BufferedImage.TYPE_BYTE_GRAY :
        {
            image.getRaster().getDataElements(0, 0, width, height, bytes);
            return bytes;
        }

        case BufferedImage.TYPE_3BYTE_BGR :
        {
            byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            int i = 0;
            if (reverseComponents) {
                for (int j = 0; j < data.length; j += 3) {
                    bytes[i++] = model.combine(data[j], data[j+1], data[j+2]);
                }
            } else {
                for (int j = 0; j < data.length; j += 3) {
                    bytes[i++] = model.combine(data[j+2], data[j+1], data[j]);
                }
            }
            return bytes;
        }
        
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
        {
            byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            int i = 0;
            for (int j = 0; j < data.length; j += 4) {
                bytes[i++] = model.combine(data[j+3], data[j+2], data[j+1]);
            }
            return bytes;
        }
        
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        case BufferedImage.TYPE_INT_RGB:
        {
            int[] data = (int[]) image.getRaster().getDataElements(0, 0, width, height, null);
            for (int i = 0; i < data.length; i++) {
                int d = data[i];
                bytes[i] = model.combine((d >> 16) & 0xff, (d >> 8) & 0xff, d & 0xff);
            }
            return bytes;
        }
        
        case BufferedImage.TYPE_USHORT_GRAY:
        {
            short[] data = (short[]) image.getRaster().getDataElements(0, 0, width, height, null);
            for (int i = 0; i < data.length; i++) {
                bytes[i] = (byte) (data[i] >> 8);
            }
            return bytes;
        }
        
        default: throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        }
    }
    
    public static byte[] toByteIntensity(BufferedImage image, IntensityModel model, byte[] bytes) {
    	return toByteIntensity(image, image.getType(), model, bytes);
    }

    public static byte[] toByteIntensity(BufferedImage image, int type, IntensityModel model) {
        return toByteIntensity(image, type, model, new byte[image.getWidth() * image.getHeight()]);
    }

    public static byte[] toByteIntensity(BufferedImage image, IntensityModel model) {
        return toByteIntensity(image, image.getType(), model);
    }

    public static byte[] extractAlpha(BufferedImage image, byte[] bytes) {
        int width = image.getWidth();
        int height = image.getHeight();
        switch(image.getType()) {
        case BufferedImage.TYPE_BYTE_GRAY :
        case BufferedImage.TYPE_3BYTE_BGR :
        case BufferedImage.TYPE_USHORT_GRAY:
        {
        	Arrays.fill(bytes, 0, width * height, (byte) 255);
        	return bytes;
        }
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
        {
            byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            for (int j = 0; j < data.length; j += 4) {
                bytes[j >> 2] = data[j+3];
            }
            return bytes;
        }
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        {
            int[] data = (int[]) image.getRaster().getDataElements(0, 0, width, height, null);
            for (int i = 0; i < data.length; i++) {
				bytes[i] = (byte) (data[i] >> 24);
			}
            return bytes;
        }
        default: throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        }
    }

    public static byte[] extractAlpha(BufferedImage image) {
    	return extractAlpha(image, new byte[image.getWidth() * image.getHeight()]);
    }
    
    public static BufferedImage fromByteIntensity(BufferedImage image, byte[] bytes) {
    	int width = image.getWidth();
    	int height = image.getHeight();
        switch(image.getType()) {
        case BufferedImage.TYPE_BYTE_BINARY:
        case BufferedImage.TYPE_BYTE_GRAY :
        {
            image.getWritableTile(0,0).setDataElements(0, 0, width, height, bytes);
            return image;
        }
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        case BufferedImage.TYPE_INT_RGB:
        {
            int[] data = new int[width * height];
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i] & 0xff;
                data[i] = (((((0xff << 8) | b) << 8) | b) << 8) | b; 
            }
            image.getWritableTile(0,0).setDataElements(0, 0, width, height, data);
            return image;
        }
        case BufferedImage.TYPE_3BYTE_BGR:
        {
            byte[] data = new byte[width * height * 3];
            int offset = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i]; 
                data[offset++] = b;
                data[offset++] = b;
                data[offset++] = b;
            }
            image.getWritableTile(0,0).setDataElements(0, 0, width, height, data);
            return image;
        }
        default: throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        
        }
    }
    
    public static BufferedImage fromByteIntensity(int width, int height, int imageType, byte[] bytes) {
    	BufferedImage image = new BufferedImage(width, height, imageType);
    	return fromByteIntensity(image, bytes);
    }

    public static BufferedImage fromByteRGB(BufferedImage image, byte[] reds, byte[] greens, byte[] blues) {
        int width = image.getWidth();
        int height = image.getHeight();
        int size = width * height;
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) throw new IllegalArgumentException();
        byte[] combined = new byte[3 * size];
        int offset = 0;
        if (reverseComponents) {
	        for (int i = 0; i < size; i++) {
	            combined[offset++] = reds[i];
	            combined[offset++] = greens[i];
	            combined[offset++] = blues[i];
	        }
        } else {
	        for (int i = 0; i < size; i++) {
	            combined[offset++] = blues[i];
	            combined[offset++] = greens[i];
	            combined[offset++] = reds[i];
	        }
        }
//        System.arraycopy(reds, 0, combined, 0, size);
//        System.arraycopy(greens, 0, combined, size, size);
//        System.arraycopy(blues, 0, combined, 2*size, size);
        image.getWritableTile(0,0).setDataElements(0,0,width,height,combined);
        return image;
    }

    public static BufferedImage fromByteRGB(int width, int height, byte[] reds, byte[] greens, byte[] blues) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        return fromByteRGB(image, reds, greens, blues);
    }

    public static int[] intFromByteRGB(byte[] reds, byte[] greens, byte[] blues) {
    	int[] pixels = new int[reds.length];
    	for (int i = 0; i < pixels.length; i++) {
			pixels[i] = ((reds[i] & 0xff) << 16) | ((greens[i] & 0xff) << 8) | (blues[i] & 0xff);
		}
    	return pixels;
    }
    
    public static int[] toIntRGB(BufferedImage image, int[] ints) {
        int width = image.getWidth();
        int height = image.getHeight();
        switch(image.getType()) {
        case BufferedImage.TYPE_BYTE_GRAY :
        {
        	byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
        	for (int i = 0; i < data.length; i++) {
				int v = data[i] & 0xff;
				ints[i] = (v << 16) | (v << 8) | v;
				
			}
            return ints;
        }

        case BufferedImage.TYPE_3BYTE_BGR :
        {
            byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            int i = 0;
            if (reverseComponents) {
	            for (int j = 0; j < data.length; j += 3) {
	                ints[i++] = ((data[j] & 0xff) << 16) | ((data[j+1] & 0xff) << 8) | (data[j+2] & 0xff);
	            }
            } else {
	            for (int j = 0; j < data.length; j += 3) {
	                ints[i++] = ((data[j+2] & 0xff) << 16) | ((data[j+1] & 0xff) << 8) | (data[j] & 0xff);
	            }
            }
            return ints;
        }
        
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
        {
            byte[] data = (byte[]) image.getRaster().getDataElements(0, 0, width, height, null);
            int i = 0;
            if (reverseComponents) {
	            for (int j = 0; j < data.length; j += 3) {
	                ints[i++] = ((data[j+1] & 0xff) << 16) | ((data[j+2] & 0xff) << 8) | (data[j+3] & 0xff);
	            }
            } else {
	            for (int j = 0; j < data.length; j += 3) {
	                ints[i++] = ((data[j+3] & 0xff) << 16) | ((data[j+2] & 0xff) << 8) | (data[j+1] & 0xff);
	            }
            }
            return ints;
        }
        
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
        case BufferedImage.TYPE_INT_RGB:
        {
            image.getRaster().getDataElements(0, 0, width, height, ints);
            return ints;
        }
        
        case BufferedImage.TYPE_USHORT_GRAY:
        {
            short[] data = (short[]) image.getRaster().getDataElements(0, 0, width, height, null);
        	for (int i = 0; i < data.length; i++) {
				int v = (data[i] & 0xffff) >> 8;
				ints[i] = (v << 16) | (v << 8) | v;
				
			}
            return ints;
        }
        
        default: throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        }
    }

    public static int[] toIntRGB(BufferedImage image) {
    	return toIntRGB(image, new int[image.getWidth() * image.getHeight()]);
    }

    public static BufferedImage fromIntRGB(BufferedImage image, int[] rgb) {
    	//TODO must support more types
    	if (image.getType() != BufferedImage.TYPE_INT_RGB) throw new IllegalArgumentException();
    	image.getWritableTile(0, 0).setDataElements(0, 0, image.getWidth(), image.getHeight(), rgb);
    	return image;
    }

    public static BufferedImage fromIntRGB(int width, int height, int[] rgb) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return fromIntRGB(image, rgb);
    }

    public static byte[][] splitIntRGB(int[] rgb) {
    	byte[] rs = new byte[ rgb.length ];
    	byte[] gs = new byte[ rgb.length ];
    	byte[] bs = new byte[ rgb.length ];
    	
    	for (int i = 0; i < rgb.length; i++) {
			int p = rgb[i];
			rs[i] = (byte) (p >> 16);
			gs[i] = (byte) (p >>  8);
			bs[i] = (byte) (p      );
		}
    	
    	return new byte[][] { rs, gs, bs };
    }

    public static BufferedImage convertImage(BufferedImage source, int type) {
    	BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), type);
    	Graphics2D g = target.createGraphics();
    	g.drawImage(source, null, null);
    	g.dispose();
    	return target;
    }

	public static JFrame showImage(String title, BufferedImage image) {
		final JFrame f = new JFrame(title);
		Container pane = f.getContentPane();
		JButton display = new JButton(new ImageIcon(image));
		display.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		display.setMargin(new Insets(50, 50, 50, 50));
		pane.add(display);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setVisible(true);
		return f;
	}

	public static BufferedImage duplicateImage(BufferedImage image) {
		BufferedImage dup = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		image.copyData(dup.getWritableTile(0, 0));
		dup.releaseWritableTile(0, 0);
		return dup;
	}

	public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
		return scaleImage(image, image.getType(), width, height);
	}

	public static BufferedImage scaleImage(BufferedImage image, int type, int width, int height) {
		BufferedImage scaled = new BufferedImage(width, height, type);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return scaled;
	}

	public static BufferedImage fromIntIntensity(int width, int height, int[] data, int shift) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		byte[] bytes = new byte[data.length];
		for (int i = 0; i < bytes.length; i++) {
			int v = Math.abs(data[i]);
			v = v >> shift;
			if (v > 255) v = 255;
			bytes[i] = (byte) v;	
		}
		image.getWritableTile(0, 0).setDataElements(0, 0, width, height, bytes);
		image.releaseWritableTile(0, 0);
		return image;
	}

	public static BufferedImage fromIntIntensityScaled(int width, int height, int[] data) {
		int max = 0;
		for (int d : data)
			if (d > max) max = d;
			else if (-d > max) max = -d;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		byte[] bytes = new byte[data.length];
		if (max > 0) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) (Math.abs(data[i]) * 255 / max);	
			}
		}
		image.getWritableTile(0, 0).setDataElements(0, 0, width, height, bytes);
		image.releaseWritableTile(0, 0);
		return image;
	}

	public static BufferedImage fromBooleans(int width, int height, boolean[] bools) {
		byte[] data = new byte[width * height];
		for (int i = 0; i < data.length; i++) {
			data[i] = bools[i] ? (byte)255 : 0;
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		image.getWritableTile(0, 0).setDataElements(0, 0, width, height, data);
		image.releaseWritableTile(0, 0);
		return image;
	}

	public static int[][] bytesToInts(byte[][] bytes) {
		int[][] ints = new int[bytes.length][];
		for (int a = 0; a < bytes.length; a++) {
			byte[] bs = bytes[a];
			int[] is = new int[bs.length];
			ints[a] = is;
			for (int i = 0; i < bs.length; i++) {
				is[i] = bs[i] & 0xff;
			}
		}
		return ints;
	}
	
	public static byte[][] intsToBytes(int[][] ints) {
		byte[][] bytes = new byte[ints.length][];
		for (int a = 0; a < ints.length; a++) {
			int[] is = ints[a];
			byte[] bs = new byte[is.length];
			bytes[a] = bs;
			for (int i = 0; i < is.length; i++) {
				int v = is[i];
				if (v < 0) v = 0;
				else if (v > 255) v = 255;
				bs[i] = (byte) v;
			}
		}
		return bytes;
	}
	
	public static void unscaledYUVToRGB(int[][] yuv) {
		int length = yuv[0].length;
		final int[] ys = yuv[0];
		final int[] us = yuv[1];
		final int[] vs = yuv[2];
		
		for (int i = 0; i < length; i++) {
			int y = ys[i];
			int u = us[i];
			int v = vs[i];
			int g = (y-u-v) >> 2;
			us[i] = g;
			ys[i] = v + g;
			vs[i] = u + g;
		}
	}

	public static void rgbToUnscaledYUV(int[][] rgb) {
		int length = rgb[0].length;
		final int[] rs = rgb[0];
		final int[] gs = rgb[1];
		final int[] bs = rgb[2];
		
		for (int i = 0; i < length; i++) {
			int r = rs[i];
			int g = gs[i];
			int b = bs[i];
			rs[i] = r + 2*g + b;
			gs[i] = b-g;
			bs[i] = r-g;
		}
	}

	public static void rgbToScaledYUV(byte[][] rgb) {
		int length = rgb[0].length;
		final byte[] rs = rgb[0];
		final byte[] gs = rgb[1];
		final byte[] bs = rgb[2];
		
		for (int i = 0; i < length; i++) {
			int r = rs[i] & 0xff;
			int g = gs[i] & 0xff;
			int b = bs[i] & 0xff;
			rs[i] = (byte) ((r + (g << 1) + b) >> 2);
			gs[i] = (byte) ((b-g)/2);
			bs[i] = (byte) ((r-g)/2);
		}
	}
	
	public static void scaledYUVToRGB(byte[][] yuv) {
		int length = yuv[0].length;
		final byte[] ys = yuv[0];
		final byte[] us = yuv[1];
		final byte[] vs = yuv[2];
		
		for (int i = 0; i < length; i++) {
			int y = ys[i] & 0xff;
			int u = us[i];
			int v = vs[i];
			int g = y - ((u+v)/2);
			int r = g + (v*2);
			int b = g + (u*2);
			//TODO verify why these overflows occur
			if (b < 0) b = 0;
			if (r < 0) r = 0;
			us[i] = (byte) g;
			ys[i] = (byte) r;
			vs[i] = (byte) b;
		}
	}
	
}
