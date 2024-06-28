package domain;

import java.math.BigDecimal;

public record SupplierInvoice(
		ClientInvoice clientInvoice, //TODO would like to not pass this.
		SupplierId supplierId,
		String invoiceDate,
		String dueDate,
		String supplierName,
		Address supplierAddress,
		InvoiceAmounts invoiceAmounts,
		String supplierReference,
		User agent,
		String supplierVatNr,
		String supplierCountryCode,
		String serialNumber,
		PaymentMethod paymentMethod,
		BigDecimal amountDue,
		Commission commission,
		VatInformationTexts vatInformationTexts
) {

}
