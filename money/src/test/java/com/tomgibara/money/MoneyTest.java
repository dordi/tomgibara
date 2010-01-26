package com.tomgibara.money;
import java.math.RoundingMode;
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
	
}
