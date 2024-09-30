package service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import facade.ClientFacade;
import facade.ClientInvoiceFacade;
import facade.PEHttpClient;

class ClientInvoiceServiceTest {

	PEHttpClient peHttpClient = new PEHttpClient();
	ClientInvoiceFacade clientInvoiceFacade = new ClientInvoiceFacade(peHttpClient);
	ClientFacade clientFacade = new ClientFacade(peHttpClient);
	ClientInvoiceService clientInvoiceService = new ClientInvoiceService(clientInvoiceFacade, clientFacade);

	@Disabled
	@Test
	void testGetUnprocessedClientInvoices() throws Exception {
		System.out.println(clientInvoiceService.getUnprocessedClientInvoices(7));
	}
}