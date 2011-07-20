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

import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

import junit.framework.TestCase;

import com.tomgibara.money.MoneyType;


public class MoneyTest extends TestCase {

	public void testToString() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals("$1.10", type.money(110).toString());
		assertEquals("$0.00", type.money(0).toString());
		assertEquals("$0.00", type.money(-0.0001).toString());
		final String negative = type.money(-5.00).toString();
		assertTrue(negative.equals("-$5.00") || negative.equals("($5.00)"));

		MoneyType de = new MoneyType(Locale.GERMANY, Currency.getInstance("EUR"));
		assertEquals("1,00 €", de.money(100).toString());

		MoneyType uk = new MoneyType(Locale.UK, Currency.getInstance("GBP"));
		assertEquals("£1.00", uk.money(100).toString());

	}
	
	public void testRounding() {
		MoneyType type = new MoneyType(Locale.US);
		assertEquals("1.10", type.money(110).getRoundedAmount().toString());
		assertEquals("1.05", type.money(1.05).getRoundedAmount().toString());
		assertEquals("1.05", type.money(1.051).getRoundedAmount().toString());
		assertEquals("1.06", type.money(1.0551).getRoundedAmount().toString());
		assertEquals("1.06", type.money(1.051).getRoundedAmount(RoundingMode.UP).toString());
	}
	
	public void testPosNeg() {
		MoneyType t = new MoneyType(Locale.US);
		Money n = t.money(-1);
		Money z = t.money(0);
		Money p = t.money(1);
		assertTrue(n.sign() < 0);
		assertFalse(n.isZero());
		assertTrue(z.sign() == 0);
		assertTrue(z.isZero());
		assertTrue(p.sign() > 0);
		assertFalse(p.isZero());
	}
	
	public void testCompareTo() {
		MoneyType us = new MoneyType(Locale.US);
		MoneyType rt = new MoneyType();
		assertTrue(us.money(100).compareTo(us.money(40)) > 0);
		assertTrue(rt.money().compareTo(us.money(40)) < 0);
		assertTrue(us.money(40).compareTo(rt.money(40)) == 0);
	}
	
}
