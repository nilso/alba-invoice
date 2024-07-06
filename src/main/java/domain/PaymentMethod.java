package domain;

import java.util.Optional;


public record PaymentMethod(String name, String number, Optional<String> additionalPaymentMethod, Optional<String> additionalNumber) {
	@Override
	public String toString() {
		return "PaymentMethod{" +
				"name='" + name + '\'' +
				", number='" + number + '\'' +
				", additionalPaymentMethod=" + additionalPaymentMethod.orElse("") +
				", additionalNumber=" + additionalNumber.orElse("") +
				'}';
	}
}
