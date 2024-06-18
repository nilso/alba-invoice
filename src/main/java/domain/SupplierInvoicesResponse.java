package domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierInvoicesResponse(@JsonProperty("supplier-invoices") List<SupplierInvoiceResponse> supplierInvoiceResponses, int offset, int limit, int size) {

}