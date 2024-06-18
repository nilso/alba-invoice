package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoiceResponse(SupplierId supplierId, String serialNumber) {
}
