/*
 * Copyright 2010 Tom Gibara
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
package com.tomgibara.crinch.hashing;

import com.tomgibara.crinch.util.WriteStream;

/**
 * Converts an object into byte data that can be used to compute a hash value.
 * 
 * @author tomgibara
 *
 * @param <T> the type of object that will be be the source of hash data
 */

//TODO rename this class and its methods, but to what?
public interface HashSource<T> {

	void sourceData(T value, WriteStream out);
	
}
