package domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoiceResponse(InvoiceId id,
									  @JsonProperty("supplier-ref") SupplierRef supplierRef,
									  @JsonProperty("reference-nr") String serialNumber,
									  @JsonProperty("self-invoice-references") ClientInvoiceIds clientInvoiceIds) {
	public record SupplierRef(@JsonProperty("id") SupplierId supplierId) {
	}

	public record ClientInvoiceIds(@JsonProperty("client-invoice-ids") List<InvoiceId> clientInvoiceReference) {
	}
}
