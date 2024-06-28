package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import domain.ClientInvoice;
import domain.InvoiceAmounts;
import domain.ProductRow;

public class InvoiceAmountCalculator {

	public static InvoiceAmounts calculateSupplierInvoiceAmounts(ClientInvoice clientInvoice) {
		BigDecimal vatRate = calculateVatRate(clientInvoice);
		BigDecimal vatAmount = calculateVatAmount(clientInvoice);
		BigDecimal grossPriceRounded = clientInvoice.grossPrice();
		BigDecimal netPrice = clientInvoice.netPrice();
		Optional<BigDecimal> roundingAmount = clientInvoice.roundingAmount();
		return new InvoiceAmounts(clientInvoice.currency(), netPrice, grossPriceRounded, vatAmount, vatRate, roundingAmount, clientInvoice.productRows());

	}

	private static BigDecimal calculateVatRate(ClientInvoice clientInvoice) {
		return clientInvoice.productRows().getFirst().vatRate();
	}

	private static BigDecimal calculateVatAmount(ClientInvoice clientInvoice) {
		BigDecimal vatAmount = BigDecimal.ZERO;
		for (ProductRow productRow : clientInvoice.productRows()) {
			BigDecimal vatRate = productRow.vatRate();
			BigDecimal netPrice = productRow.netPrice();
			BigDecimal vat = netPrice.multiply(vatRate);
			vatAmount = vatAmount.add(vat);
		}
		return vatAmount.setScale(2, RoundingMode.HALF_UP);
	}
}
