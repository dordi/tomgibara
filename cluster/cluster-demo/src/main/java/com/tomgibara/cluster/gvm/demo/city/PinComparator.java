/*
 * Created on 09-Aug-2007
 */
package com.tomgibara.cluster.gvm.demo.city;

import java.util.Comparator;


class PinComparator implements Comparator<Pin> {
    
    public int compare(Pin pin1, Pin pin2) {
    	int z1 = pin1.getZ();
    	int z2 = pin2.getZ();
        if (z1 == z2) {
            long uid1 = pin1.getUid();
            long uid2 = pin2.getUid();
			if (uid1 == uid2) {
                return 0;
            } else {
                return uid1 < uid2 ? -1 : 1;
            }
        } else {
            return z1 < z2 ? -1 : 1;
        }
    }
    
}