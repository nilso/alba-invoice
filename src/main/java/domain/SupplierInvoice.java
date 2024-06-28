package domain;

import java.math.BigDecimal;

public record SupplierInvoice(
		SupplierId supplierId,
		String supplierName,
		Address supplierAddress,
		ClientInvoice clientInvoice,
		String supplierReference,
		User agent,
		String supplierVatNr,
		String supplierCountryCode,
		String serialNumber,
		PaymentMethod paymentMethod,
		BigDecimal vatRate,
		BigDecimal vatAmount,
		BigDecimal amountDue,
		Commission commission,
		VatInformationTexts vatInformationTexts
) {

}
