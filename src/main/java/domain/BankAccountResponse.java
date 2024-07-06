package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BankAccountResponse(
		BankAccountId id,
		String name,
		String currency

) {
}
