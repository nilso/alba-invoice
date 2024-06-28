package util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import domain.Commission;

class CommissionCalculatorTest {

	@Test
	void calculateCommission_SE() {
		Commission commission = CommissionCalculator.calculateCommission(new BigDecimal("12347.67"), new BigDecimal("0.1000"), "SE");
		assertEquals(new BigDecimal("1234.77"), commission.netCommission());
		assertEquals(new BigDecimal("1543.46"), commission.grossCommission());
		assertEquals(new BigDecimal("0.25"), commission.commissionVatRate());
		assertEquals(new BigDecimal("308.69"), commission.commissionVatAmount());
		assertEquals(new BigDecimal("0.1000"), commission.commissionRate());
		assertTrue(commission.commissionRoundingAmount().isPresent());
		assertEquals(new BigDecimal("0.46"), commission.commissionRoundingAmount().get());
	}

	@Test
	void calculateCommission_EU() {
		Commission commission = CommissionCalculator.calculateCommission(new BigDecimal("12347.67"), new BigDecimal("0.1000"), "DK");
		assertEquals(new BigDecimal("1234.77"), commission.netCommission());
		assertEquals(new BigDecimal("1234.77"), commission.grossCommission());
		assertEquals(new BigDecimal("0"), commission.commissionVatRate());
		assertEquals(new BigDecimal("0.00"), commission.commissionVatAmount());
		assertEquals(new BigDecimal("0.1000"), commission.commissionRate());
		assertTrue(commission.commissionRoundingAmount().isPresent());
		assertEquals(new BigDecimal("0.77"), commission.commissionRoundingAmount().get());
	}

}