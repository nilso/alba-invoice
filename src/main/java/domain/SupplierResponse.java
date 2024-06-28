package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record SupplierResponse(
		@JsonProperty("id") SupplierId id,
		@JsonProperty("country-code") String countryCode,
		String name,
		Address address,
		String plusgiro,
		String bankgiro,
		@JsonProperty("swedish-bank-account") String swedishBankAccount,
		@JsonProperty("international-bank-account-with-routing") String internationalBankAccountWithRouting,
		@JsonProperty("international-bank-account-with-account-number") String internationalBankAccountWithAccountNumber,
		@JsonProperty("international-bank-account-via-intermediary") String internationalBankAccountViaIntermediary,
		@JsonProperty("international-bank-account-in-russia-in-rub") String internationalBankAccountInRussiaInRub,
		@JsonProperty("norwegian-bank-account") String norwegianBankAccount,
		String iban,
		@JsonProperty("vat-nr") String vatNr,
		@JsonProperty("our-reference") String ourReference
) {
}
