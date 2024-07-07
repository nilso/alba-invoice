package facade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import config.Config;
import domain.HttpMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PEHttpClient {
	public PEHttpClient() {
	}

	public String httpGet(String endpoint) throws Exception {
		return makeRequest(endpoint, "", HttpMethod.GET);
	}

	private String makeRequest(String endpoint, String body, HttpMethod method) throws Exception {
		String apiUrl = Config.getBaseUrl() + endpoint;
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.uri(URI.create(apiUrl))
				.header("Content-type", "application/json")
				.header("X-Token", Config.getApiKey());

		if (method == HttpMethod.PUT) {
			builder.PUT(HttpRequest.BodyPublishers.ofString(body));
		} else if (method == HttpMethod.GET) {
			if (!body.isEmpty()) {
				throw new IllegalArgumentException("GET requests cannot have a body");
			}
			builder.GET();
		} else if (method == HttpMethod.POST) {
			builder.POST(HttpRequest.BodyPublishers.ofString(body));
		} else {
			throw new IllegalArgumentException("Unsupported HTTP method");
		}

		try {
			HttpClient client = getClient();
			log.info("Making request to {}", apiUrl);
			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
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

	public void httpPut(String endpoint, String body) throws Exception {
		makeRequest(endpoint, body, HttpMethod.PUT);
	}

	public void httpPost(String endpoint, String body) throws Exception {
		makeRequest(endpoint, body, HttpMethod.POST);
	}
}
