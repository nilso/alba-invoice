package domain;

import lombok.Builder;

@Builder()
public class SupplierResponseTestBuilder {
	@Builder.Default
	SupplierId id = new SupplierId("1");
	@Builder.Default
	String countryCode = "SE";
	@Builder.Default
	String name = "Supplier";
	@Builder.Default
	Address address = AddressTestBuilder.builder().build().anAddress();
	@Builder.Default
	String plusgiro = "";
	@Builder.Default
	String bankgiro = "";
	@Builder.Default
	String swedishBankAccount = "";
	@Builder.Default
	String internationalBankAccountWithRouting = "";
	@Builder.Default
	String internationalBankAccountWithAccountNumber = "";
	@Builder.Default
	String internationalBankAccountViaIntermediary = "";
	@Builder.Default
	String internationalBankAccountInRussiaInRub = "";
	@Builder.Default
	String norwegianBankAccount = "";
	@Builder.Default
	String iban = "";
	@Builder.Default
	String vatNr = "SE1234567890";
	@Builder.Default
	String ourReference = "OurReference";
	@Builder.Default
	SupplierResponse.BankAccount bankAccount = new SupplierResponse.BankAccount(new BankAccountId("1"));

	public SupplierResponse aResponse() {
		return new SupplierResponse(id,
				countryCode,
				name,
				address,
				plusgiro,
				bankgiro,
				swedishBankAccount,
				internationalBankAccountWithRouting,
				internationalBankAccountWithAccountNumber,
				internationalBankAccountViaIntermediary,
				internationalBankAccountInRussiaInRub,
				norwegianBankAccount,
				iban,
				vatNr,
				ourReference,
				bankAccount);
	}
}
