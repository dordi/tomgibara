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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

/**
 * <p>
 * Combines a currency with a locale for the purpose of recording and formatting
 * monetary amounts. Instances of this class are immutable.
 * </p>
 * 
 * <p>
 * If a null locale is supplied for a type, then any money values of that type
 * will be formatted with the currency formatter specified by the default
 * platform locale.
 * </p>
 * 
 * @author Tom Gibara
 * 
 */

public class MoneyType implements MoneySource, MoneyCalcOrigin {

	// fields
	
	//may be null if not specified
	final Currency currency;
	
	//may be null if not specified
	final Locale locale;
	
	//TODO a thread local might be better
	final NumberFormat format;
	final int places;
	final Money zeroMoney;
	
	// constructors

	/**
	 * Construct a new type with no specific locale or currency.
	 */
	
	public MoneyType() {
		this(null, null);
	}
	
	/**
	 * Construct a new type with a specific locale. The currency will be the
	 * default currency for the supplied locale (where supplied).
	 * 
	 * @param locale
	 *            the locale for the type, may be null
	 */

	public MoneyType(Locale locale) {
		this(locale, locale == null ? null : Currency.getInstance(locale));
	}

	/**
	 * Construct a new type a specific currency but no specific locale.
	 * 
	 * @param currency
	 *            the currency for the type, may be null
	 */

	public MoneyType(Currency currency) {
		this(null, currency);
	}
	
	/**
	 * Construct a new type with a specific locale and currency.
	 * 
	 * @param locale
	 *            the locale for the type, may be null
	 * @param currency
	 *            the currency for the type, may be null
	 */

	public MoneyType(Locale locale, Currency currency) {
		this.locale = locale;
		this.currency = currency;
		this.format = locale == null ? NumberFormat.getCurrencyInstance() : NumberFormat.getCurrencyInstance(locale);
		this.places = format.getMaximumFractionDigits();
		this.zeroMoney = new Money(this, BigDecimal.ZERO);
	}
	
	// accessors
	
	/**
	 * The currency for this type.
	 * 
	 * @return a currency or null
	 */
	
	public Currency getCurrency() {
		return currency;
	}
	
	/**
	 * The locale for this type.
	 * 
	 * @return a locale or null
	 */
	
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * The number of decimal places in this currencies presentation.
	 * 
	 * @return the number of decimal places
	 */
	
	public int getPlaces() {
		return places;
	}
	
	// public methods
	
	/**
	 * Converts the supplied value (eg. cents) into a monetary amount.
	 * 
	 * @param smallDenomination
	 *            a monetary value in small denominations
	 * @return a corresponding monetary amount
	 */

	public Money money(int smallDenomination) {
		return new Money(this, smallDenomination);
	}
	
	/**
	 * Converts the supplied value (eg. cents) into a monetary amount.
	 * 
	 * @param smallDenomination
	 *            a monetary value in small denominations
	 * @return a corresponding monetary amount
	 */

	public Money money(BigInteger smallDenomination) {
		return new Money(this, smallDenomination);
	}
	
	/**
	 * Converts the supplied value (eg. dollars) into a monetary amount.
	 * 
	 * @param largeDenomination
	 *            a monetary value in large denominations
	 * @return a corresponding monetary amount
	 */
	
	public Money money(double largeDenomination) {
		return new Money(this, largeDenomination);
	}
	
	/**
	 * Converts the supplied value (eg. dollars) into a monetary amount.
	 * 
	 * @param largeDenomination
	 *            a monetary value in large denominations
	 * @return a corresponding monetary amount
	 */
	
	public Money money(BigDecimal largeDenomination) {
		return new Money(this, largeDenomination);
	}

	/**
	 * Parses a string into a monetary amount according the locale for this type
	 * (or the default system locale if none was specified).
	 * 
	 * @param string
	 *            a formatted currency value
	 * @return the corresponding monetary amount
	 * @throws IllegalArgumentException
	 *             if the string could not be parsed
	 */

	public Money parse(String string) throws IllegalArgumentException {
		synchronized (format) {
			try {
				return new Money(this, new BigDecimal( format.parse(string).toString() ));
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	/**
	 * A convenience method that returns a zero monetary amount.
	 * 
	 * @return a monetary amount of zero
	 */
	
	public Money money() {
		return zeroMoney;
	}

	/**
	 * A convenience method that creates a new monetary calculation with the
	 * initial value of zero.
	 * 
	 * @return a new monetary calculation initialized to zero
	 */

	@Override
	public MoneyCalc calc() {
		return new MoneyCalc(this, BigDecimal.ZERO);
	}

	/**
	 * A convenience method that creates a new monetary calculation with the
	 * initial value of zero.
	 * 
	 * @return a new monetary calculation initialized to zero
	 */

	@Override
	public MoneyCalc calc(int scale, RoundingMode roundingMode) {
		return new MoneyCalc(scale, roundingMode, this, BigDecimal.ZERO);
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
			//TODO this is lazy
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
