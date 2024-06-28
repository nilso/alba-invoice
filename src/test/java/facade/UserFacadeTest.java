package facade;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import domain.User;

class UserFacadeTest {

	@Disabled
	@Test
	void fetchUserName() throws Exception {
		UserFacade userFacade = new UserFacade(new PEHttpClient());
		assertEquals(new User(92446, "Nils Österling"), userFacade.fetchUserName(92446));
	}

}