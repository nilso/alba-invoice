package domain;

import java.util.Optional;

public record TableData(
		ClientInvoice clientInvoice,
		User user,
		Optional<Supplier> supplier,
		Optional<SerialNumber> serialNumber
) {
}
