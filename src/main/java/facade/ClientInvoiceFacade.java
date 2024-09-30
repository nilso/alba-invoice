package facade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.ClientInvoiceResponse;
import domain.ClientInvoicesResponse;

public class ClientInvoiceFacade {
	private final PEHttpClient peHttpClient;

	public ClientInvoiceFacade(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
	}

	public List<ClientInvoiceResponse> fetchClientInvoices(int daysBack) throws Exception {
		String dateFilter = getFormattedDate(daysBack);
		String endpoint = String.format("/company/%s/client/invoice?filter=all&invoiceDateLower=%s", Config.getClientId(), dateFilter);

		String body = peHttpClient.httpGet(endpoint);

		ObjectMapper objectMapper = new ObjectMapper();
		ClientInvoicesResponse clientInvoicesResponse = objectMapper.readValue(body, ClientInvoicesResponse.class);
		return clientInvoicesResponse.clientInvoices();
	}

	public List<ClientInvoiceResponse> fetchClientInvoicesById(String id) throws Exception {
		String endpoint = String.format("/company/%s/client/invoice/%s", Config.getClientId(), id);

		String body = peHttpClient.httpGet(endpoint);

		ObjectMapper objectMapper = new ObjectMapper();
		return List.of(objectMapper.readValue(body, ClientInvoiceResponse.class));
	}

	private String getFormattedDate(int daysBack) {
		LocalDate date = LocalDate.now().minusDays(daysBack);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return date.format(formatter);
	}
}
