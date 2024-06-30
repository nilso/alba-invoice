package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domain.ClientInvoice;
import domain.ClientInvoiceTestBuilder;
import domain.InvoiceId;
import domain.Supplier;
import domain.SupplierResponse;
import domain.SupplierResponseTestBuilder;
import facade.PEHttpClient;
import facade.SupplierFacade;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {
	String bankGiroNumber = "12345432";
	String bankGiroNumberExpected = "1234-5432";
	@Mock
	SupplierFacade supplierFacade;

	@InjectMocks
	SupplierService supplierService;

	@Disabled
	@Test
	void getSupplierMap_bankgiro() throws Exception {
		String invoiceNr = "12345";
		ClientInvoice clientInvoice = ClientInvoiceTestBuilder.builder()
				.invoiceNr(invoiceNr)
				.build()
				.aClientInvoice();

		SupplierResponse supplierResponse = SupplierResponseTestBuilder.builder()
				.bankgiro(bankGiroNumber)
				.build().aResponse();
		when(supplierFacade.fetchSupplier(any())).thenReturn(supplierResponse);

		Map<InvoiceId, Supplier> supplierMap = supplierService.getSupplierMap(List.of(clientInvoice));

		assertEquals(1, supplierMap.size());
		Supplier result = supplierMap.get(new InvoiceId(invoiceNr));
		assertEquals("Bankgiro", result.paymentMethod().name());
		assertEquals(bankGiroNumberExpected, result.paymentMethod().number());
	}

	@Disabled
	@Test
	void fetchAllSuppliers() throws Exception {
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierService supplierService = new SupplierService(new SupplierFacade(peHttpClient));
		System.out.println(supplierService.getAllSuppliers());
	}
}