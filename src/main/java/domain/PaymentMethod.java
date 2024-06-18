package domain;

import java.util.Optional;

public record PaymentMethod(String name, String number, Optional<String> additionalPaymentMethod, Optional<String> additionalNumber) {
}
