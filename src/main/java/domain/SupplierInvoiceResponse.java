package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoiceResponse(String id, @JsonProperty("supplier-ref") SupplierRef supplierRef, @JsonProperty("reference-nr") String serialNumber) {
	public record SupplierRef(@JsonProperty("id") SupplierId supplierId) {
	}
}
