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
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Instances of this class accumulate interim calculation values on monetary
 * amounts. NOTE: Operations on this class mutate the object value, consequently
 * the class is not safe for concurrent multithreaded use.
 * </p>
 * 
 * <p>
 * Because instances of this class are mutable, calculations will generally be
 * performed more efficiently than if {@link Money} objects are being realized
 * at each computational step.
 * </p>
 * 
 * <p>
 * Each operation on the calculation returns the same calculation so that
 * repeated calls to one object may be chained. As operations are performed as
 * part of a calculation, an attempt is made to reconcile the type of each
 * monetary amount with that of the calculation, if this is not possible (eg.
 * adding dollars to pounds sterling) an exception is raised.
 * </p>
 * 
 * <p>
 * Calculations operate at arbitrary precision.
 * </p>
 * 
 * @author Tom Gibara
 * 
 */

public class MoneyCalc implements MoneyCalcOrigin {

	// statics

	/**
	 * The default rounding mode used to limit precision in calculations.
	 */
	
	public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
	
	/* Invariant: when scale is non-negative, amount always has specified scale */

	// fields

	private final int scale;
	private final RoundingMode roundingMode;

	private MoneyType type;
	private BigDecimal amount;
	
	// constructors

	MoneyCalc(int scale, RoundingMode roundingMode, MoneyType type, BigDecimal amount) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (amount == null) throw new IllegalArgumentException("null amount");
		this.scale = scale < 0 ? -1 : scale;
		this.roundingMode = roundingMode == null ? DEFAULT_ROUNDING_MODE : roundingMode;
		this.type = type;
		this.amount = scaled(amount);
	}

	MoneyCalc(MoneyType type, BigDecimal amount) {
		this(-1, null, type, amount);
	}

	
	// accessors
	
	/**
	 * The rounding mode applied at each step in the calculation.
	 * 
	 * @return the rounding mode
	 */
	
	public RoundingMode getRoundingMode() {
		return roundingMode;
	}
	
	/**
	 * The number of decimal digits to which the calculation is rounded at every
	 * step.
	 * 
	 * @return the decimal scale for the calculation, -1 if no scale is applied.
	 */
	
	public int getScale() {
		return scale;
	}
	
	/**
	 * The amount thus far computed.
	 * 
	 * @return the computed amount
	 */
	
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * The monetary type of this calculation.
	 * 
	 * @return the monetary type
	 */
	
	public MoneyType getType() {
		return type;
	}
	
	/**
	 * Directly change the amount of the calculation. 
	 * 
	 * @param amount any amount of money
	 */
	
	public void setAmount(BigDecimal amount) {
		if (amount == null) throw new IllegalArgumentException("null amount");
		this.amount = amount;
	}

	/**
	 * Directly change the type of the calculation
	 * 
	 * @param type any monetary type
	 */
	
	public void setType(MoneyType type) {
		if (type == null) throw new IllegalArgumentException("null type");
		this.type = type;
	}

	// methods

	/**
	 * Obtains the result of this calculation as a monetary amount. A
	 * calculation object may continue to be used after this method is called.
	 * Each call will return a {@link Money} object that represents the value of
	 * the calculation at the time of the call.
	 * 
	 * @return the result of this calculation.
	 */
	
	public Money money() {
		return new Money(type, amount);
	}

	
	/**
	 * Obtains the result of this calculation evenly split into an array of
	 * monetary amounts. This method can only called if a scale has been set for
	 * this calculation, otherwise an IllegalStateException arises.
	 * 
	 * Where the monetary amount cannot be split evenly, at the defined scale,
	 * into the specified number of parts, the method minimizes the maximum
	 * absolute error over the returned monetary amounts, under the constraint
	 * that their sum is exactly equal to the value of the calculation.
	 * 
	 * @param parts
	 *            the number of parts into which the calculation value should be
	 *            split (at least one)
	 * @return an array of monetary values that, as far as is possible, evenly
	 *         split the value of the calculation
	 * @throws IllegalStateException
	 *             if no scale has been defined for the calculation
	 */

	public Money[] moneySplit(int parts) throws IllegalStateException {
		if (scale < 0) throw new IllegalStateException("no scale set");
		if (parts < 1) throw new IllegalArgumentException("parts not positive");
		Money[] monies = new Money[parts];
		BigDecimal remainder = amount;
		for (int i = parts - 1; i >= 0; i--) {
			if (i == 0) {
				monies[i] = new Money(type, remainder);
			} else {
				BigDecimal share = remainder.divide(BigDecimal.valueOf(i + 1), scale, roundingMode);
				monies[i] = new Money(type, share);
				remainder = remainder.subtract(share);
			}
		}
		return monies;
	}
	
	public Money[] moneySplit(BigDecimal... proportions) throws IllegalStateException {
		if (scale < 0) throw new IllegalStateException("no scale set");
		final int count = proportions.length;
		switch (count) {
		case 0 : throw new IllegalArgumentException("no proportions");
		case 1 : return new Money[] { money() };
		default :
			BigDecimal[] denominators = new BigDecimal[count];
			{
				BigDecimal denominator = null;
				for (int i = 0; i < count; i++) {
					denominator = i == 0 ? proportions[0] : denominator.add(proportions[i]);
					denominators[i] = denominator;
				}
			}
			Money[] monies = new Money[count];
			BigDecimal remainder = amount;
			for (int i = count - 1; i >= 0; i--) {
				if (i == 0) {
					monies[i] = new Money(type, remainder);
				} else {
					BigDecimal share = remainder.multiply(proportions[i]).divide(denominators[i], scale, roundingMode);
					monies[i] = new Money(type, share);
					remainder = remainder.subtract(share);
				}
			}
			return monies;
		}
	}
	
	/**
	 * Adds a monetary amount to this calculation.
	 * 
	 * @param money
	 *            the monetary amount to add
	 * @return the current calculation object
	 * @throws IllegalArgumentException
	 *             if the type of the money supplied cannot be reconciled
	 *             with the type of the calculation
	 */
	
	public MoneyCalc add(Money money) throws IllegalArgumentException {
		type = type.combine(money.type);
		amount = amount.add(scaledAmount(money));
		return this;
	}

	/**
	 * Subtracts a monetary amount from this calculation.
	 * 
	 * @param money
	 *            the monetary amount to subtract
	 * @return the current calculation object
	 * @throws IllegalArgumentException
	 *             if the type of the money supplied cannot be reconciled
	 *             with the type of the calculation
	 */
	
	public MoneyCalc subtract(Money money) {
		type = type.combine(money.type);
		amount = amount.subtract(scaledAmount(money));
		return this;
	}
	
	/**
	 * Multiplies the current calculation amount by the supplied value.
	 * 
	 * @param value
	 *            the multiplicand
	 * @return the current calculation object
	 */
	
	public MoneyCalc multiply(BigDecimal value) {
		amount = amount.multiply(value);
		if (scale >= 0 && value.scale() == 0) amount = amount.setScale(scale, roundingMode);
		return this;
	}
	
	/**
	 * Divides the current calculation amount by the supplied value. If no scale
	 * has been set for this calculation, then the value must divide without
	 * loss of precision, otherwise an ArithmeticException may be raised.
	 * 
	 * @param value
	 *            the multiplicand
	 * @return the current calculation object
	 * @throws ArithmeticException
	 *             if the division cannot be performed under the specified
	 *             rounding mode
	 */
	
	public MoneyCalc divide(BigDecimal value) throws ArithmeticException {
		if (scale >= 0) {
			amount = amount.divide(value, scale, roundingMode);
		} else {
			amount = amount.divide(value);
		}
		return this;
	}
	
	/**
	 * Takes the maximum of the current calculation value and the supplied
	 * monetary amount.
	 * 
	 * @param money
	 *            a monetary amount
	 * @return the current calculation object
	 * @throws IllegalArgumentException
	 *             if the type of the money supplied cannot be reconciled with
	 *             the type of the calculation
	 */
	
	public MoneyCalc max(Money money) {
		type = type.combine(money.type);
		amount = amount.max(scaledAmount(money));
		return this;
	}
	
	/**
	 * Takes the minimum of the current calculation value and the supplied
	 * monetary amount.
	 * 
	 * @param money
	 *            a monetary amount
	 * @return the current calculation object
	 * @throws IllegalArgumentException
	 *             if the type of the money supplied cannot be reconciled with
	 *             the type of the calculation
	 */
	
	public MoneyCalc min(Money money) {
		type = type.combine(money.type);
		amount = amount.min(scaledAmount(money));
		return this;
	}
	
	/**
	 * Takes the absolute value of the current calculation value.
	 * 
	 * @return the current calculation object
	 */
	
	public MoneyCalc abs() {
		amount = amount.abs();
		return this;
	}
	
	/**
	 * Negates the value of the current calculation value.
	 * 
	 * @return the current calculation object
	 */
	
	public MoneyCalc negate() {
		amount = amount.negate();
		return this;
	}

	// calc origin methods
	
	/**
	 * Opens a new calculation whose initial type and amount are initially equal
	 * to the current values of this calculation.
	 * 
	 * @return a new monetary calculation
	 */
	
	@Override
	public MoneyCalc calc() {
		return new MoneyCalc(this.type, this.amount);
	}
	
	/**
	 * Opens a new calculation whose initial type and amount are initially equal
	 * to the current values of this calculation.
	 * 
	 * @return a new monetary calculation
	 */
	
	@Override
	public MoneyCalc calc(int scale, RoundingMode roundingMode) {
		return new MoneyCalc(scale, roundingMode, this.type, this.amount);
	}
	
	// object methods

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MoneyCalc)) return false;
		MoneyCalc that = (MoneyCalc) obj;
		if (this.scale != that.scale) return false;
		if (this.roundingMode != that.roundingMode) return false;
		if (!this.type.equals(that.type)) return false;
		if (this.amount.compareTo(that.amount) != 0) return false;
		return true;
	}
	
	public int hashCode() {
		return scale ^ roundingMode.hashCode() ^ type.hashCode() ^ amount.hashCode();
	}
	
	public String toString() {
		return type.format(money());
	}
	
	// private utility methods
	
	private BigDecimal scaledAmount(Money money) {
		return scaled(money.amount);
	}
	
	private BigDecimal scaled(BigDecimal amount) {
		return scale >= 0 ? amount.setScale(scale, roundingMode) : amount;
	}
	
}
