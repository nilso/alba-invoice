package domain;

import lombok.Builder;

@Builder()
public class ClientTestBuilder {
	@Builder.Default
	ClientId id = new ClientId(1081025);
	@Builder.Default
	String name = "Test Company";
	@Builder.Default
	String vatNumber = "SE556036079701";
	@Builder.Default
	String orgNo = "556036-0797";
	@Builder.Default
	String countryCode = "SE";

	public Client aClient() {
		return Client.builder()
				.id(id)
				.name(name)
				.vatNumber(vatNumber)
				.orgNo(orgNo)
				.countryCode(countryCode)
				.build();

	}
}
