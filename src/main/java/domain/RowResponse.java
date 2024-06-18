package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RowResponse(
		double price,
		@JsonProperty("vat") double vatRate,
		Product product,
		String description
) {
}
