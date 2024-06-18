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

	public static String bigDecimalToPercent(BigDecimal number) {
		return number.multiply(new BigDecimal("100"))
				.setScale(0, RoundingMode.HALF_UP)
				.toString();
	}
}
