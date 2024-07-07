package service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import facade.PEHttpClient;
import facade.SupplierInvoiceFacade;

class SupplierInvoiceServiceTest {

	@Mock
	private SupplierInvoiceFacade supplierInvoiceFacade;

	@Disabled
	@Test
	public void actuallyGet() throws Exception {
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierInvoiceFacade supplierInvoiceFacade = new SupplierInvoiceFacade(peHttpClient);
		SupplierInvoiceService supplierInvoiceService = new SupplierInvoiceService(supplierInvoiceFacade);

		supplierInvoiceService.getAllSupplierInvoicesOneYearBack();
	}

}