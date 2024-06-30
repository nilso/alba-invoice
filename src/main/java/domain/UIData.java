package domain;

import java.util.Optional;

public record UIData(
		ClientInvoice clientInvoice,
		User user,
		Supplier supplier,
		Optional<SerialNumber> serialNumber
) {
}
