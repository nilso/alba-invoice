package domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public record ClientInvoice(
		InvoiceId id,
		Client client,
		String invoiceNr,
		ReferenceId ourReferenceId,
		String yourReference,
		Address invoiceAddress,
		List<ProductRow> productRows,
		String invoiceDate,
		String dueDate,
		BigDecimal grossPrice,
		Optional<BigDecimal> roundingAmount,
		BigDecimal netPrice,
		BigDecimal vatAmount,
		String currency,
		BigDecimal commissionRate,
		SupplierId supplierId
) {
}
