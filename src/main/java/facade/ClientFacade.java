package facade;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.ClientId;
import domain.ClientResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFacade {
	private final PEHttpClient peHttpClient;

	public ClientFacade(PEHttpClient peHttpClient) {

		this.peHttpClient = peHttpClient;
	}

	public ClientResponse fetchClientById(ClientId clientId) throws Exception {
		String endpoint = String.format("/company/%s/client/%s", Config.getClientId(), clientId.getId());

		String response = peHttpClient.httpCall(endpoint);
		log.info(response);
		ObjectMapper objectMapper = new ObjectMapper();
		ClientResponse client = objectMapper.readValue(response, ClientResponse.class);
		log.info("Client fetched: {}", client);
		return client;
	}
}
