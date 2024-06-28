package domain;

import lombok.Builder;

@Builder
public record Client(
		ClientId id,
		String name,
		String vatNumber,
		String orgNo,
		String countryCode
) {
}
