package domain;

import lombok.Builder;

@Builder()
public class AddressTestBuilder {
	@Builder.Default
	String address1 = "address1";
	@Builder.Default
	String address2 = "address2";
	@Builder.Default
	String zipCode = "zipCode";
	@Builder.Default
	String state = "state";
	@Builder.Default
	String country = "SE";

	public Address anAddress() {
		return Address.builder()
				.address1(address1)
				.address2(address2)
				.zipCode(zipCode)
				.state(state)
				.country(country)
				.build();
	}
}
