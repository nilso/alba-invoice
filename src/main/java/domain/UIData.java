package domain;

import java.util.Optional;

public record UIData(
		ClientInvoice clientInvoice,
		User user,
		Optional<Supplier> supplier,
		Optional<SerialNumber> serialNumber
) {
	public UIData withSupplier(Supplier supplier) {
		return new UIData(clientInvoice, user, Optional.of(supplier), serialNumber);
	}
}
