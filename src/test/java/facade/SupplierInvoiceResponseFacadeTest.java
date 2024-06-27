package facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import domain.SupplierInvoicesResponse;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceResponseFacadeTest {

	@Mock
	ObjectMapper objectMapper;
	@Mock
	private PEHttpClient peHttpClient;
	@InjectMocks
	private SupplierInvoiceFacade supplierInvoiceFacade;

	@Test
	void testFetchByFinancialYear() throws Exception {
		LocalDate firstOfSeptemberAtLeastOneYearAgo = SupplierInvoiceFacade.getOneYearBack();
		String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), firstOfSeptemberAtLeastOneYearAgo);
		String responseBody = "{\"size\": 1, \"supplierInvoices\": [{\"supplierId\": \"1\", \"serialNumber\": \"alba01-01\"}]}";

		when(peHttpClient.httpGet(endpoint)).thenReturn(responseBody);

		SupplierInvoiceResponse invoice = new SupplierInvoiceResponse(new SupplierId("1"), "alba01-01");
		SupplierInvoicesResponse supplierInvoicesResponse = new SupplierInvoicesResponse(List.of(invoice), 0, 0, 1);

		when(objectMapper.readValue(responseBody, SupplierInvoicesResponse.class)).thenReturn(supplierInvoicesResponse);

		Map<SupplierId, List<SupplierInvoiceResponse>> result = supplierInvoiceFacade.fetchInvoicesOneYearBack();
		assertEquals(1, result.size());
		assertEquals(1, result.get(new SupplierId("1")).size());
	}

	@Test
	void testFetchByFinancialYearNoInvoices() throws Exception {
		LocalDate firstOfSeptemberAtLeastOneYearAgo = SupplierInvoiceFacade.getOneYearBack();
		String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), firstOfSeptemberAtLeastOneYearAgo);
		String responseBody = "{\"size\": 0, \"supplierInvoices\": []}";

		when(peHttpClient.httpGet(endpoint)).thenReturn(responseBody);

		SupplierInvoicesResponse supplierInvoicesResponse = new SupplierInvoicesResponse(List.of(), 0, 0, 0);
		when(objectMapper.readValue(responseBody, SupplierInvoicesResponse.class)).thenReturn(supplierInvoicesResponse);

		Map<SupplierId, List<SupplierInvoiceResponse>> result = supplierInvoiceFacade.fetchInvoicesOneYearBack();
		assertTrue(result.isEmpty());
	}
}
