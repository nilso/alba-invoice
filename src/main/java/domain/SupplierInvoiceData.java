package domain;

import java.math.BigDecimal;

public record SupplierInvoiceData(
		ClientInfo clientInfo,
		SupplierInfo supplierInfo,
		String invoiceDate,
		String dueDate,
		InvoiceAmounts invoiceAmounts,
		User agent,
		String serialNumber,
		PaymentMethod paymentMethod,
		BigDecimal amountDue,
		Commission commission,
		VatInformationTexts vatInformationTexts
) {
	public record ClientInfo(String name, Address invoiceAddress, String countryCode, String orgNo, String vatNumber, String invoiceNr) {

	}

	public record SupplierInfo(SupplierId id, String name, Address address, String reference, String vatNr, String countryCode) {
	}
}
