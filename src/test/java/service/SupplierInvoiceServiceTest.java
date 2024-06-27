package service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import domain.Commission;

class SupplierInvoiceServiceTest {


	private static final SupplierInvoiceService supplierInvoiceService = new SupplierInvoiceService();

	@Test
	void createCommission() {
		Commission commission = supplierInvoiceService.createCommission(new BigDecimal("12347.67"), new BigDecimal("0.1000"), new BigDecimal("0.06"));
		assertEquals(new BigDecimal("1234.77"), commission.netCommission());
		assertEquals(new BigDecimal("1308.86"), commission.grossCommission());
		assertEquals(new BigDecimal("0.06"), commission.commissionVatRate());
		assertEquals(new BigDecimal("74.09"), commission.commissionVatAmount());
		assertEquals(new BigDecimal("0.1000"), commission.commissionRate());
		assertTrue(commission.commissionRoundingAmount().isPresent());
		assertEquals(new BigDecimal("0.86"), commission.commissionRoundingAmount().get());
	}

}