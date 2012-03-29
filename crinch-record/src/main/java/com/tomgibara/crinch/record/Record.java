/*
 * Copyright 2011 Tom Gibara
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.tomgibara.crinch.record;

public interface Record {

	/**
	 * The number of the record within a sequence of records.
	 * 
	 * @return the ordinal or -1L if not known
	 */
	
	long getOrdinal();
	
	/**
	 * The position at which the record is persisted.
	 * 
	 * @return the position or -1L if not known
	 */
	
	long getPosition();
	
	/**
	 * Called to indicate that the record object is no longer required.
	 * No methods should be called on the record after this method has
	 * been called.
	 */
	
	void release();
	
}
