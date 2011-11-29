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

public class SubRecordDefinition {

	private static final int[] NO_INDICES = new int[0];
	private static final List<ColumnOrder.Indexed> NO_ORDERS = Collections.emptyList();
	
	final boolean ordinalEliminated;
	final boolean positionEliminated;
	final int[] indices;
	final List<ColumnOrder.Indexed> orders;
	
	public SubRecordDefinition(boolean ordinalEliminated, boolean positionEliminated, int[] indices, List<ColumnOrder.Indexed> orders) {
		this.ordinalEliminated = ordinalEliminated;
		this.positionEliminated = positionEliminated;
		//TODO should check arguments as far as possible
		this.indices = indices == null ? NO_INDICES : indices.clone();
		this.orders = orders == null ? NO_ORDERS : Collections.unmodifiableList(new ArrayList<ColumnOrder.Indexed>(orders));
	}
	
	public boolean isOrdinalEliminated() {
		return ordinalEliminated;
	}
	
	public boolean isPositionEliminated() {
		return positionEliminated;
	}
	
	public int[] getIndices() {
		return indices.clone();
	}
	
	public int getColumnCount() {
		return indices.length;
	}
	
	public List<ColumnOrder.Indexed> getOrders() {
		return orders;
	}
}
