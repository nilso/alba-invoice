package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientResponse(
		@JsonProperty("id") ClientId id,
		String name,
		@JsonProperty("vat-nr") String vatNumber,
		@JsonProperty("orgno") String orgNo,
		@JsonProperty("country-code") String countryCode
) {
}
