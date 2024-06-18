package facade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import config.Config;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PEHttpClient {
	public PEHttpClient() {
	}

	public String httpCall(String endpoint) throws Exception {

		String apiUrl = Config.getBaseUrl() + endpoint;
		try {
			HttpClient client = getClient();
			log.info("Making request to {}", apiUrl);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(apiUrl))
					.header("Content-type", "application/json")
					.header("X-Token", Config.getApiKey())
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("Response body: {}", response.body());
			return response.body();
		} catch (Exception e) {
			log.error("Error creating HttpClient");
			throw e;
		}
	}

	private static HttpClient getClient() {
		return HttpClient.newHttpClient();
	}
}
