package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class BigDecimalUtil {
	public static Optional<BigDecimal> extractDecimalPartIfNotZero(BigDecimal number) {
		BigDecimal remainder = number.remainder(BigDecimal.ONE);
		if (remainder.compareTo(BigDecimal.ZERO) == 0) {
			return Optional.empty();
		}

		return Optional.of(remainder);
	}

	public static BigDecimal roundToWholeNumberButKeepTwoDecimals(BigDecimal number) {
		return number.setScale(0, RoundingMode.HALF_UP)
				.setScale(2, RoundingMode.FLOOR);
	}

	public static String doubleToPercent(double number) {
		return bigDecimalToPercent(BigDecimal.valueOf(number));
	}

	public static String bigDecimalToPercent(BigDecimal number) {
		BigDecimal result = number.multiply(new BigDecimal("100"));
		if (result.stripTrailingZeros().scale() > 0) {
			result = result.setScale(2, RoundingMode.HALF_UP);
		}
		return result.stripTrailingZeros().toPlainString();
	}

	public static double parsePercentStringToDecimal(String percentString) {
		percentString = percentString.replace(",", ".");
		double percentValue = Double.parseDouble(percentString);
		return percentValue / 100;
	}

	public static BigDecimal doubleToBigDecimal(double value) {
		return BigDecimal.valueOf(value)
				.setScale(2, RoundingMode.HALF_UP);
	}
}
