package domain;

import java.util.Optional;

import javax.annotation.Nullable;

public record Supplier(
		SupplierId id,
		String countryCode,
		String name,
		String supplierReference,
		PaymentMethod paymentMethod,
		Address address,
		String vatNr,
		Optional<BankAccountId> bankAccountId
) {
}
