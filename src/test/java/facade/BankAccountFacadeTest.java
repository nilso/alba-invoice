package facade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class BankAccountFacadeTest {

	@Disabled
	@Test
	void actuallyFetchesBankAccount() throws Exception {
		PEHttpClient peHttpClient = new PEHttpClient();
		BankAccountFacade facade = new BankAccountFacade(peHttpClient);
		System.out.println(facade.fetchBankAccounts());

	}

}