package domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientInvoicesResponse(@JsonProperty("client-invoices") List<ClientInvoiceResponse> clientInvoices, int offset, int limit, int size) {

}