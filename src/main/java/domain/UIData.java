package domain;

import java.util.Optional;

public record UIData(
		ClientInvoice clientInvoice,
		User user,
		Optional<Supplier> supplier,
		Optional<SerialNumber> serialNumber
) {
}
