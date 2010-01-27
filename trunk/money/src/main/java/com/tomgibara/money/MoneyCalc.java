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

public class MoneyCalc {

	// fields
	
	private MoneyType type;
	private BigDecimal amount;
	
	// constructors

	MoneyCalc(MoneyType type, BigDecimal amount) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (amount == null) throw new IllegalArgumentException("null amount");
		this.type = type;
		this.amount = amount;
	}
	
	// accessors
	
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
		amount = amount.add(money.amount);
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
		amount = amount.subtract(money.amount);
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
		return this;
	}
	
	/**
	 * Divides the current calculation amount by the supplied value.
	 * 
	 * @param value
	 *            the multiplicand
	 * @return the current calculation object
	 */
	
	public MoneyCalc divide(BigDecimal value) {
		amount = amount.divide(value);
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
		amount = amount.max(money.amount);
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
		amount = amount.min(money.amount);
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

	// object methods

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
	
	public String toString() {
		return type.format(money());
	}
	
}
