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
package com.tomgibara.crinch.record.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubRecordDef {

	private final boolean ordinalRetained;
	private final boolean positionRetained;
	private final int[] indices;
	private final List<ColumnOrder.Indexed> orders;

	public SubRecordDef(boolean ordinalRetained, boolean positionRetained) {
		this(ordinalRetained, positionRetained, null, null);
	}

	public SubRecordDef(int[] indices) {
		this(true, true, indices, null);
	}

	public SubRecordDef(List<ColumnOrder.Indexed> orders) {
		this(true, true, null, orders);
	}

	public SubRecordDef(boolean ordinalRetained, boolean positionRetained, int[] indices, List<ColumnOrder.Indexed> orders) {
		this.ordinalRetained = ordinalRetained;
		this.positionRetained = positionRetained;
		//TODO should check arguments as far as possible
		this.indices = indices == null ? null : indices.clone();
		this.orders = orders == null ? null : Collections.unmodifiableList(new ArrayList<ColumnOrder.Indexed>(orders));
	}
	
	public boolean isOrdinalRetained() {
		return ordinalRetained;
	}
	
	public boolean isPositionRetained() {
		return positionRetained;
	}
	
	public int[] getIndices() {
		return indices == null ? null : indices.clone();
	}
	
	public List<ColumnOrder.Indexed> getOrders() {
		return orders;
	}
	
	int[] getIndicesUnsafely() {
		return indices;
	}
}
