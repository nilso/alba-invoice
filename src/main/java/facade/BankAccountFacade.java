package facade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.BankAccount;
import domain.BankAccountId;
import domain.BankAccountResponse;
import domain.BankAccountResponses;
import domain.ClientInvoiceResponse;
import domain.ClientInvoicesResponse;

public class BankAccountFacade {
	private final PEHttpClient peHttpClient;

	public BankAccountFacade(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
	}

	public Map<BankAccountId, BankAccount> fetchBankAccounts() throws Exception {
		String endpoint = String.format("/company/%s/bank/account", Config.getClientId());

		String body = peHttpClient.httpGet(endpoint);

		ObjectMapper objectMapper = new ObjectMapper();
		BankAccountResponses bankAccountResponses = objectMapper.readValue(body, BankAccountResponses.class);
		return bankAccountResponses.bankAccounts().stream()
				.map(bankAccountResponse -> new BankAccount(
						bankAccountResponse.id(),
						bankAccountResponse.name(),
						bankAccountResponse.currency()
				))
				.collect(Collectors.toMap(BankAccount::id, bankAccount -> bankAccount));
	}
}
