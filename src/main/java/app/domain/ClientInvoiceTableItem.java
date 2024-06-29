package app.domain;

public record ClientInvoiceTableItem(
		String id,
		String clientName,
		String invoiceNr,
		double grossPrice,
		double commissionRate
) {
}