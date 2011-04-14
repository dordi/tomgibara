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
package com.tomgibara.cluster;

/**
 * Number type used as a placeholder for java primitives.
 * 
 * @author Tom Gibara
 */

public class NUMBER extends Number {

	private static final long serialVersionUID = -3674103110014680482L;
	
	private static NUMBER ZERO = new NUMBER(0.0);
	
    public static NUMBER zero() {
    	return ZERO;
    }

    public static NUMBER number(double value) {
    	return new NUMBER(value);
    }
    
    public static NUMBER sum(NUMBER n1, NUMBER n2) {
        return new NUMBER(n1.value + n2.value);
    }
    
    public static NUMBER difference(NUMBER n1, NUMBER n2) {
        return new NUMBER(n1.value - n2.value);
    }
    
    public static NUMBER product(NUMBER n1, NUMBER n2) {
        return new NUMBER(n1.value * n2.value);
    }

    public static NUMBER quotient(NUMBER n1, NUMBER n2) {
        return new NUMBER(n1.value / n2.value);
    }
    
    public static NUMBER square(NUMBER n1) {
        return product(n1, n1);
    }
    
    public static void add(NUMBER[] array, int index, NUMBER n) {
        NUMBER m = array[index];
        array[index] = NUMBER.sum(m, n);
    }

    public static double doubleValue(NUMBER n) {
    	return n.doubleValue();
    }
    
    public static boolean equal(NUMBER n1, NUMBER n2) {
        return n1.value == n2.value;
    }
    
    public static boolean unequal(NUMBER n1, NUMBER n2) {
        return n1.value != n2.value;
    }
    
    public static boolean lessThan(NUMBER n1, NUMBER n2) {
        return n1.value < n2.value;
    }

    // number implementation

    private double value;
    
    private NUMBER(double value) {
    	this.value = value;
    }
    
    @Override
    public double doubleValue() {
    	return value;
    }
    
    @Override
    public float floatValue() {
    	return (float) value;
    }
    
    @Override
    public int intValue() {
    	return (int) value;
    }
    
    @Override
    public long longValue() {
    	return (long) value;
    }
    
}
