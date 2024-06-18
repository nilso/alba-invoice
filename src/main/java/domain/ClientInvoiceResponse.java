package domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientInvoiceResponse(
		@JsonProperty("id") InvoiceId id,
		@JsonProperty("client-ref") ClientId clientId,
		boolean certified,
		@JsonProperty("invoice-nr") String invoiceNr,
		@JsonProperty("our-reference") ReferenceId ourReference,
		@JsonProperty("your-reference") String yourReference,
		@JsonProperty("invoice-address") Address invoiceAddress,
		RowsResponse rows,
		@JsonProperty("invoice-date") String invoiceDate,
		@JsonProperty("due-date") String dueDate,
		double amount,
		double vat,
		String currency,
		Fields fields
) {
	public record Fields(List<Field> fields) {
	}
}
