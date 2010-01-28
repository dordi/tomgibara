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
package com.tomgibara.money;

import java.math.BigDecimal;
import java.util.Locale;

import junit.framework.TestCase;

public class MoneyCalcTest extends TestCase {

	public void testSimpleCalculations() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals(type.money(100), type.money(80).calc().add(type.money(20)).money());
		assertEquals(type.money(60), type.money(80).calc().subtract(type.money(20)).money());
		assertEquals(type.money(800), type.money(80).calc().multiply(BigDecimal.TEN).money());
		assertEquals(type.money(8), type.money(80).calc().divide(BigDecimal.TEN).money());
		assertEquals(type.money(80), type.money(80).calc().max(type.money(20)).money());
		assertEquals(type.money(20), type.money(80).calc().min(type.money(20)).money());
		assertEquals(type.money(80), type.money(-80).calc().abs().money());
		assertEquals(type.money(-80), type.money(80).calc().negate().money());
	}
	
	public void testTypeCombining() {
		MoneyType a = new MoneyType(Locale.US, null);
		MoneyType b = new MoneyType(Locale.US);
		assertEquals(b, a.calc().add(b.calc().money()).getType());
		MoneyType c = new MoneyType(Locale.UK);
		try {
			b.calc().add(c.calc().money()).getType();
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}
	}
	
}
