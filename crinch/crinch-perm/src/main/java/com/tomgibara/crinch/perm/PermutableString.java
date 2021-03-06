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
package com.tomgibara.crinch.perm;

public class PermutableString implements Permutable {

	private final StringBuilder sb;
	
	public PermutableString(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		sb = new StringBuilder(str);
	}
	
	public PermutableString(StringBuilder sb) {
		if (sb == null) throw new IllegalArgumentException("null sb");
		this.sb = sb;
	}
	
	
	public StringBuilder getStringBuilder() {
		return sb;
	}
	
	@Override
	public int getPermutableSize() {
		return sb.length();
	}
	
	@Override
	public Permutable transpose(int i, int j) {
		char c = sb.charAt(i);
		sb.setCharAt(i, sb.charAt(j));
		sb.setCharAt(j, c);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
