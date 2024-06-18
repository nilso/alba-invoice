package domain;

public record Supplier(
		SupplierId id,
		String countryCode,
		String name,
		String supplierReference,
		PaymentMethod paymentMethod,
		Address address,
		String vatNr
) {
}
