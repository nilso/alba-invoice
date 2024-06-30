package domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.Builder;

@Builder
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
		Optional<BigDecimal> commissionRate,
		SupplierId supplierId
) {
	public ClientInvoice withUITableData(BigDecimal commissionRate) {
		return ClientInvoice.builder()
				.id(id)
				.client(client)
				.invoiceNr(invoiceNr)
				.ourReferenceId(ourReferenceId)
				.yourReference(yourReference)
				.invoiceAddress(invoiceAddress)
				.productRows(productRows)
				.invoiceDate(invoiceDate)
				.dueDate(dueDate)
				.grossPrice(grossPrice)
				.roundingAmount(roundingAmount)
				.netPrice(netPrice)
				.vatAmount(vatAmount)
				.currency(currency)
				.commissionRate(Optional.of(commissionRate))
				.supplierId(supplierId)
				.build();

	}
}
