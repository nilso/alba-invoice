package facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import config.Config;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceFacadeTest {
	@Mock
	private PEHttpClient peHttpClient;
	@InjectMocks
	private SupplierInvoiceFacade supplierInvoiceFacade;

	@Test
	void testFetchByFinancialYearNoInvoices() throws Exception {
		LocalDate firstOfSeptemberAtLeastOneYearAgo = SupplierInvoiceFacade.getOneYearBack();
		String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), firstOfSeptemberAtLeastOneYearAgo);
		String responseBody = "{\"size\": 0, \"supplierInvoices\": []}";

		when(peHttpClient.httpGet(endpoint)).thenReturn(responseBody);

		Map<SupplierId, List<SupplierInvoiceResponse>> result = supplierInvoiceFacade.fetchInvoicesOneYearBack();
		assertTrue(result.isEmpty());
	}

	@Disabled
	@Test
	void actuallyFetch() throws Exception {
		PEHttpClient realClient = new PEHttpClient();
		SupplierInvoiceFacade realFacade = new SupplierInvoiceFacade(realClient);
		System.out.println(realFacade.fetchInvoicesOneYearBack());
	}
}
