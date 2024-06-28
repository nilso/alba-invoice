package facade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import domain.ClientId;

class ClientFacadeTest {

	@Disabled
	@Test
	void fetchClientById() throws Exception {
		ClientFacade clientFacade = new ClientFacade(new PEHttpClient());
		clientFacade.fetchClientById(new ClientId(1081025));
	}
}