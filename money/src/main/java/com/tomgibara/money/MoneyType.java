package com.tomgibara.money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

public class MoneyType {

	// fields
	
	//may be null if not specified
	final Currency currency;
	
	//may be null if not specified
	final Locale locale;
	
	final NumberFormat format;
	final int places;
	
	// constructors
	
	public MoneyType() {
		this(null, null);
	}
	
	public MoneyType(Locale locale) {
		this(locale, locale == null ? null : Currency.getInstance(locale));
	}
	
	public MoneyType(Currency currency) {
		this(null, currency);
	}

	public MoneyType(Locale locale, Currency currency) {
		this.locale = locale;
		this.currency = currency;
		this.format = locale == null ? NumberFormat.getCurrencyInstance() : NumberFormat.getCurrencyInstance(locale);
		this.places = format.getMaximumFractionDigits();
	}
	
	// accessors
	
	public Currency getCurrency() {
		return currency;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	// public methods
	
	public Money money(int smallDenomination) {
		return new Money(this, smallDenomination);
	}
	
	public Money money(BigInteger smallDenomination) {
		return new Money(this, smallDenomination);
	}
	
	public Money money(double largeDenomination) {
		return new Money(this, largeDenomination);
	}
	
	public Money money(BigDecimal largeDenomination) {
		return new Money(this, largeDenomination);
	}
	
	public Money money(String string) {
		synchronized (format) {
			try {
				return new Money(this, new BigDecimal( format.parse(string).toString() ));
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	//convenience method
	public Money money() {
		return new Money(this, BigDecimal.ZERO);
	}
	
	//convenience method
	public MoneyCalc calc() {
		return new MoneyCalc(this, BigDecimal.ZERO);
	}
	
	// object methods
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MoneyType)) return false;
		MoneyType that = (MoneyType) obj;
		if (this.currency != that.currency) return false;
		if (this.locale == that.locale) return true;
		if (this.locale == null || that.locale == null) return false;
		return this.locale.equals(that.locale);
	}
	
	public int hashCode() {
		int h = 0;
		if (currency != null) h ^= currency.hashCode();
		if (locale != null) h ^= locale.hashCode();
		return h;
	}

	// package methods
	
	String format(Money money) {
		synchronized (format) {
			return format.format(money.getRoundedAmount());
		}
	}
	
	MoneyType combine(MoneyType that) {
		if (this == that) return this;
		//identify a common currency (poss. null)
		Currency currency;
		if (this.currency == that.currency) {
			currency = this.currency;
		} else if (this.currency == null) {
			currency = that.currency;
		} else if (that.currency == null) {
			currency = this.currency;
		} else {
			throw new IllegalArgumentException("Incompatible currencies: " + this.currency + " " + that.currency);
		}
		//identify a common locale
		Locale locale;
		if (this.locale == that.locale) {
			locale = this.locale;
		} else if (this.locale == null) {
			locale = that.locale;
		} else if (that.locale == null) {
			locale = this.locale;
		} else if (this.locale.equals(that.locale)) {
			locale = this.locale;
		} else {
			String strThis = this.locale.getCountry() + '_' + this.locale.getLanguage();
			String strThat = that.locale.getCountry() + '_' + that.locale.getLanguage();
			if (strThis.startsWith(strThat)) {
				locale = this.locale;
			} else if (strThat.startsWith(strThis)) {
				locale = that.locale;
			} else {
				throw new IllegalArgumentException("Incompatible locales: " + this.locale + " " + that.locale);
			}
		}
		
		if (currency == this.currency && locale.equals(this.locale)) return this;
		if (currency == that.currency && locale.equals(that.locale)) return that;
		return new MoneyType(locale, currency);
	}
	
}
