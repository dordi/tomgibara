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
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
	
	public void testZero() {
		MoneyType type = new MoneyType(Locale.US);
		MoneyCalc calc = type.calc(3, null);
		assertTrue(calc.money().isZero());
		calc.add(type.money(1));
		assertFalse(calc.money().isZero());
		calc.zero();
		assertTrue(calc.money().isZero());
		assertEquals(3, calc.money().getAmount().scale());
	}
	
	public void testArrayCalculations() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals(type.money(6), type.calc().add(type.money(1), type.money(2), type.money(3)).money());
		assertEquals(type.money(0), type.calc().subtract(type.money(6)).add(type.money(1), type.money(2), type.money(3)).money());
		assertEquals(type.money(-6), type.calc().subtract(type.money(1), type.money(2), type.money(3)).money());
		assertEquals(type.money(0), type.calc().add(type.money(6)).subtract(type.money(1), type.money(2), type.money(3)).money());
	}
	
	public void testArrayNotPartiallyApplied() {
		MoneyType type1 = new MoneyType(Locale.US);
		MoneyType type2 = new MoneyType(Locale.UK);
		{
			Money money = type1.money(20);
			MoneyCalc calc = money.calc();
			try {
				calc.add(type2.money(10), type2.money(10));
				fail();
			} catch (IllegalArgumentException e) {
				/* expected */
			}
			assertEquals(money, calc.money());
		}
		
		MoneyType type3 = new MoneyType(new Locale("", "CA"), null);
		MoneyType type4 = new MoneyType(new Locale("fr", "CA"), null);
		{
			Money money = type3.money(20);
			MoneyCalc calc = money.calc();
			try {
				calc.add(type4.money(10), type2.money(10));
				fail();
			} catch (IllegalArgumentException e) {
				/* expected */
			}
			assertEquals(type3, calc.getType());
		}
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

	public void testScaling() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals(type.money(100), type.money(120).calc(0, null).money());
		assertEquals(type.money(), type.calc(0, RoundingMode.DOWN).add(type.money(50)).add(type.money(50)).money());
		assertEquals(type.money(33), type.money(100).calc(2, null).divide(BigDecimal.valueOf(3)).money());
		assertEquals(type.money(10), type.money(11).calc(1, null).min(type.money(12)).money() );
	}
	
	public void testGetScale() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals(-1, type.calc().getScale());
		assertEquals(0, type.calc(0, null).getScale());
		assertEquals(1, type.calc(1, null).getScale());
		assertEquals(-1, type.calc(-2, null).getScale());
	}
	
	public void testOwnMoneySource() {
		MoneyType type = new MoneyType();
		final Money money = type.money(10);
		
		MoneySource source = new MoneySource() {
			
			@Override
			public Money money() {
				return money;
			}
		};
		
		assertEquals(money, type.calc().add(source).money());
	}
	
}
