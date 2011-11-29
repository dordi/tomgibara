/*
 * Copyright 2011 Tom Gibara
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
package com.tomgibara.crinch.bits;

import java.util.Random;

public abstract class AbstractByteBasedBitReaderTest extends AbstractBitReaderTest {

	abstract ByteBasedBitReader readerFor(BitVector vector);
	
	public void testSetPosition() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = 8 + 8 * r.nextInt(100);
			BitVector source = new BitVector(r, size);
			ByteBasedBitReader reader = readerFor(source);

			for (int j = 0; j < 100; j++) {
				int position = r.nextInt(size);
				try {
					reader.setPosition(position);
				} catch (IllegalArgumentException e) {
					// backward seek not supported
					reader = readerFor(source);
					reader.setPosition(position);
				}
				assertEquals("at bit " + position, source.getBit(position), reader.readBoolean());
			}
		}
	}

}
