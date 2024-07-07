package facade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ClientInvoiceFacadeTest {

	@Disabled
	@Test
	void actuallyFetch() throws Exception {
		ClientInvoiceFacade clientInvoiceFacade = new ClientInvoiceFacade(new PEHttpClient());
		System.out.println(clientInvoiceFacade.fetchClientInvoices(30));
	}
}