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
package com.tomgibara.crinch.perm.permutable;

import com.tomgibara.crinch.perm.Permutable;

public class PermutableInterval<P extends Permutable> implements Permutable {

	private final P permutable;
	private final int offset;
	private final int length;
	
	public PermutableInterval(P permutable, int offset, int length) {
		if (permutable == null) throw new IllegalArgumentException("null permutable");
		if (length < 0) throw new IllegalArgumentException("negative length");
		if (offset < 0) throw new IllegalArgumentException("negative offset");
		if (offset + length > permutable.getPermutableSize()) throw new IllegalArgumentException("size exceeded");
		
		this.permutable = permutable;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public int getPermutableSize() {
		return length;
	}
	
	@Override
	public Permutable transpose(int i, int j) {
		permutable.transpose(i + offset, j + offset);
		return this;
	}
	
}
