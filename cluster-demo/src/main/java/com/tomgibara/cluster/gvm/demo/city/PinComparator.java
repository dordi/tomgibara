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