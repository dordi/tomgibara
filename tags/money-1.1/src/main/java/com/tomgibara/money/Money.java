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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * <p>
 * A class that stores a monetary amount. Amounts are stored as BigDecimal
 * values against a {@link MoneyType}. Instances of this class are immutable and
 * safe for multithreaded use.
 * </p>
 * 
 * <p>
 * Note: although the money amounts may be formatted to the nearest small
 * denomination, the actual amounts are stored to an arbitrary degree of
 * precision.
 * </p>
 * 
 * @author Tom Gibara
 * 
 */

public class Money implements MoneyCalcOrigin, Comparable<Money> {

	// statics
	
	/**
	 * The default rounding mode used when rounding monetary values.
	 */

	/*
	 * This mode has been chosen on the basis that it is familiar and easily
	 * reasoned about.
	 */
	
	public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
	
	// fields
	
	final MoneyType type;
	final BigDecimal amount;
	private volatile BigDecimal roundedAmount = null;
	
	// constructors
	
	Money(MoneyType type, int value) {
		this.type = type;
		this.amount = new BigDecimal(value).movePointLeft(type.places);
	}
	
	Money(MoneyType type, BigInteger value) {
		this.type = type;
		this.amount = new BigDecimal(value, type.places);
	}
	
	Money(MoneyType type, double value) {
		this.type = type;
		this.amount = new BigDecimal(value);
	}
	
	Money(MoneyType type, BigDecimal amount) {
		this.type = type;
		this.amount = amount;
	}
	
	// accessors

	/**
	 * The type of this money.
	 * 
	 * @return the type of this money
	 */
	
	public MoneyType getType() {
		return type;
	}
	
	/**
	 * The monetary amount.
	 * 
	 * @return an amount of money
	 */
	
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * The amount of money rounded to the number of decimal places dictated by
	 * the currency format of the money type.
	 * 
	 * @return the monetary amount rounded to the nearest small denomination
	 */
	
	public BigDecimal getRoundedAmount() {
		if (roundedAmount == null) {
			roundedAmount = amount.setScale(type.places, DEFAULT_ROUNDING);
		}
		return roundedAmount;
	}

	/**
	 * The amount of money rounded to the number of decimal places dictated by
	 * the currency format of the money type.
	 * 
	 * @param mode
	 *            the rounding rule to apply
	 * 
	 * @return the monetary amount rounded to the nearest small denomination
	 */
	
	public BigDecimal getRoundedAmount(RoundingMode mode) {
		if (mode == null) throw new IllegalArgumentException("null mode");
		if (mode == DEFAULT_ROUNDING) return getRoundedAmount();
		return amount.setScale(type.places, mode);
	}

	/**
	 * Whether the monetary amount is zero.
	 * 
	 * @return true iff the monetary amount is zero
	 */
	
	public boolean isZero() {
		return amount.signum() == 0;
	}

	/**
	 * The sign of the monetary amount: -1 if the amount is negative, 1 if it is
	 * positive and 0 if it is zero.
	 * 
	 * @return the sign of the monetary amount
	 */
	
	public int sign() {
		return amount.signum();
	}
	
	// calc origin methods
	
	/**
	 * Opens a calculation whose initial type and amount will match those of
	 * this object.
	 * 
	 * @return a new monetary calculation
	 */
	
	@Override
	public MoneyCalc calc() {
		return new MoneyCalc(type, amount);
	}
	
	
	/**
	 * Opens a calculation whose initial type and amount will match those of
	 * this object.
	 * 
	 * @return a new monetary calculation
	 */

	@Override
	public MoneyCalc calc(int scale, RoundingMode roundingMode) {
		return new MoneyCalc(scale, roundingMode, type, amount);
	}
	
	// object methods
	
	/**
	 * Compares the value of two monetary amounts.
	 * 
	 * @param a
	 *            money object to compare with
	 * @return a comparison as per the contract for {@link Comparable}
	 * @throws IllegalArgumentException
	 *             if the monetary types have different currencies
	 */
	
	public int compareTo(Money that) throws IllegalArgumentException {
		final Currency currencyThis = this.type.currency;
		final Currency currencyThat = that.type.currency;
		if (currencyThis != null && currencyThat != null && currencyThis != currencyThat) throw new IllegalArgumentException("Incompatible currencies " + currencyThis + " " + currencyThat);
		return this.amount.compareTo(that.amount);
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Money)) return false;
		Money that = (Money) obj;
		if (!this.type.equals(that.type)) return false;
		if (this.amount.compareTo(that.amount) != 0) return false;
		return true;
	}
	
	public int hashCode() {
		return type.hashCode() ^ amount.hashCode();
	}
	
	/**
	 * Presents the monetary amount as per the format dictated by the locale of
	 * the type (or the default locale if none is specified).
	 * 
	 * @return the money as a currency formatted string
	 */
	
	public String toString() {
		return type.format(this);
	}
	
}
