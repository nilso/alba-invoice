package domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record Address(String address1,
					  String address2,
					  @JsonProperty("zip-code") String zipCode,
					  String state,
					  String country) {
}
