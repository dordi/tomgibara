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

import java.awt.Color;


public class Pin {

	// statics
	
    private static long nextUID = 0;
    
    synchronized static long nextUID() {
        long ret = nextUID;
        nextUID++;
        if (nextUID < 0) nextUID = 0;
        return ret;
    }
    
    // fields

    private long uid = nextUID();
    private int radius = 0;
    private float x = 0;
    private float y = 0;
    private int z = 0;
    private String label;
    private Color color;
    private boolean visible = true;
    private Pin child;
    
    // accessors
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getX() {
        return x;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    
    public void setZ(int z) {
        this.z = z;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public Pin getChild() {
		return child;
	}
    
    public void setChild(Pin child) {
		this.child = child;
	}
    
    // package methods
    
    long getUid() {
		return uid;
	}
    
    void setUid(long uid) {
		this.uid = uid;
	}
    
}