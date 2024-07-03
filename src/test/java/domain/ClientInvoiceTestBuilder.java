package domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.Builder;

@Builder()
public class ClientInvoiceTestBuilder {
	@Builder.Default
	InvoiceId id = new InvoiceId(12345);
	@Builder.Default
	Client client = ClientTestBuilder.builder().build().aClient();
	@Builder.Default
	String invoiceNr = "123456";
	@Builder.Default
	ReferenceId ourReference = new ReferenceId(12345);
	@Builder.Default
	String yourReference = "yourReference";
	@Builder.Default
	Address invoiceAddress = AddressTestBuilder.builder().build().anAddress();
	@Builder.Default
	List<ProductRow> productRows = List.of(ProductRowTestBuilder.builder().build().aProductRow());
	@Builder.Default
	String invoiceDate = "2021-01-01";
	@Builder.Default
	String dueDate = "2021-01-01";
	@Builder.Default
	BigDecimal grossPrice = new BigDecimal("100.00");
	@Builder.Default
	BigDecimal roundingAmount = new BigDecimal("0.00");
	@Builder.Default
	BigDecimal netPrice = new BigDecimal("80.00");
	@Builder.Default
	BigDecimal vatAmount = new BigDecimal("20.00");
	@Builder.Default
	String currency = "SEK";
	@Builder.Default
	BigDecimal commissionRate = new BigDecimal("0.00");
	@Builder.Default
	SupplierId supplierId = new SupplierId(12345);

	public ClientInvoice aClientInvoice() {
		return ClientInvoice.builder()
				.id(id)
				.client(client)
				.invoiceNr(invoiceNr)
				.ourReferenceId(ourReference)
				.yourReference(yourReference)
				.invoiceAddress(invoiceAddress)
				.productRows(productRows)
				.invoiceDate(invoiceDate)
				.dueDate(dueDate)
				.grossPrice(grossPrice)
				.roundingAmount(Optional.of(roundingAmount))
				.netPrice(netPrice)
				.vatAmount(vatAmount)
				.currency(currency)
				.commissionRate(Optional.of(commissionRate))
				.supplierId(Optional.of(supplierId))
				.build();
	}
}
