package com.tomgibara.money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Money {

	// statics
	
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
	
	public MoneyType getType() {
		return type;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getRoundedAmount() {
		if (roundedAmount == null) {
			roundedAmount = amount.setScale(type.places, DEFAULT_ROUNDING);
		}
		return roundedAmount;
	}

	public BigDecimal getRoundedAmount(RoundingMode mode) {
		if (mode == null) throw new IllegalArgumentException("null mode");
		if (mode == DEFAULT_ROUNDING) return getRoundedAmount();
		return amount.setScale(type.places, mode);
	}

	// public methods
	
	public MoneyCalc calc() {
		return new MoneyCalc(type, amount);
	}
	
	// object methods
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Money)) return false;
		Money that = (Money) obj;
		if (!this.type.equals(that.type)) return false;
		if (!this.amount.equals(that.amount)) return false;
		return true;
	}
	
	public int hashCode() {
		return type.hashCode() ^ amount.hashCode();
	}
	
	public String toString() {
		return type.format(this);
	}
	
}
