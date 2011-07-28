package com.tomgibara.money;

import java.math.RoundingMode;

public interface MoneyCalcSource {

	/**
	 * Open a new monetary calculation. No rounding will be applied by the
	 * calculation values.
	 * 
	 * @return a new monetary calculation
	 */

	MoneyCalc calc();
	
	
	/**
	 * Open a new monetary calculation. At each step in the calculation, the
	 * value will be limited to the number of decimal places specified by the
	 * scale parameter. The rounding mode is optional and defaults to
	 * {@link MoneyCalc.DEFAULT_ROUNDING_MODE} if null is specified.
	 * 
	 * @param scale
	 *            a limit to the precision of calculations, or any negative
	 *            value
	 * @param roundingMode
	 *            the rounding mode used to limit precision, or null for the
	 *            default value
	 * 
	 * @return a new monetary calculation
	 */

	MoneyCalc calc(int scale, RoundingMode roundingMode);

	
}
