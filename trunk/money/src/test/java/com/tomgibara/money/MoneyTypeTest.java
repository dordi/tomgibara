package com.tomgibara.money;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Locale;

import junit.framework.TestCase;

import com.tomgibara.money.MoneyType;

public class MoneyTypeTest extends TestCase {

	public void testConstructor() {
		MoneyType us = new MoneyType(Locale.US);
		assertEquals(Currency.getInstance(Locale.US), us.getCurrency());
	}
	
	public void testCombine() {
		MoneyType basic = new MoneyType(null, null);
		MoneyType us = new MoneyType(Locale.US);
		MoneyType uk = new MoneyType(Locale.UK);
		assertEquals(basic, basic.combine(basic));
		assertEquals(us, basic.combine(us));
		assertEquals(us, us.combine(basic));
		assertEquals(us, us.combine(us));
		try {
			us.combine(uk);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		MoneyType a = new MoneyType(null, Currency.getInstance(Locale.US));
		MoneyType b = new MoneyType(Locale.UK, null);
		assertEquals(new MoneyType(Locale.UK, Currency.getInstance(Locale.US)), a.combine(b));

		MoneyType c = new MoneyType(new Locale("", "CA"), null);
		MoneyType d = new MoneyType(new Locale("fr", "CA"), null);
		assertEquals(d, d.combine(c));
		assertEquals(d, c.combine(d));

	}
	
	public void testMoney() {
		MoneyType us = new MoneyType(Locale.US);
		assertEquals("1.00", us.money(100).getRoundedAmount().toString());
		assertEquals("1.00", us.money(1.00).getRoundedAmount().toString());
		assertEquals("1.00", us.money(BigInteger.valueOf(100L)).getRoundedAmount().toString());
		assertEquals("1.00", us.money(new BigDecimal(1.0)).getRoundedAmount().toString());
	}
	
}
