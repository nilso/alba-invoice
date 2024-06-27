package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class BigDecimalUtilTest {

	@Test
	void bigDecimalToPercent() {
		BigDecimal number = new BigDecimal("0.1000");
		String expected = "10";
		assertEquals(expected, BigDecimalUtil.bigDecimalToPercent(number));
	}

	@Test
	void extractDecimalPartIfNotZero() {
		BigDecimal number = new BigDecimal("123.456");
		BigDecimal expected = new BigDecimal("0.456");
		assertTrue(BigDecimalUtil.extractDecimalPartIfNotZero(number).isPresent());
		assertEquals(expected, BigDecimalUtil.extractDecimalPartIfNotZero(number).get());
	}
}