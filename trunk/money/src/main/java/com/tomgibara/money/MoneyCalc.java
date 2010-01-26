package com.tomgibara.money;

import java.math.BigDecimal;

//WARNING MUTABLE!
public class MoneyCalc {

	// statics
	
	public static final MoneyType DEFAULT_TYPE = new MoneyType(null, null);
	
	// fields
	
	private MoneyType type;
	private BigDecimal amount;
	
	// constructors

	public MoneyCalc() {
		type = DEFAULT_TYPE;
		amount = BigDecimal.ZERO;
	}

	public MoneyCalc(MoneyType type, BigDecimal amount) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (amount == null) throw new IllegalArgumentException("null amount");
		this.type = type;
		this.amount = amount;
	}
	
	// accessors
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public MoneyType getType() {
		return type;
	}
	
	public void setAmount(BigDecimal amount) {
		if (amount == null) throw new IllegalArgumentException("null amount");
		this.amount = amount;
	}

	public void setType(MoneyType type) {
		if (type == null) throw new IllegalArgumentException("null type");
		this.type = type;
	}

	// methods
	
	public Money money() {
		return new Money(type, amount);
	}
	
	public MoneyCalc add(Money money) {
		type = type.combine(money.type);
		amount = amount.add(money.amount);
		return this;
	}

	public MoneyCalc subtract(Money money) {
		type = type.combine(money.type);
		amount = amount.subtract(money.amount);
		return this;
	}
	
	public MoneyCalc multiply(BigDecimal value) {
		amount = amount.multiply(value);
		return this;
	}
	
	public MoneyCalc divide(BigDecimal value) {
		amount = amount.divide(value);
		return this;
	}
	
	public MoneyCalc max(Money money) {
		type = type.combine(money.type);
		amount = amount.max(money.amount);
		return this;
	}
	
	public MoneyCalc min(Money money) {
		type = type.combine(money.type);
		amount = amount.min(money.amount);
		return this;
	}
	
	public MoneyCalc abs() {
		amount = amount.abs();
		return this;
	}
	
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
