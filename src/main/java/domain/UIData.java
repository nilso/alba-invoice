package domain;

public record UIData(
		ClientInvoice clientInvoice,
		User user,
		Supplier supplier,
		SerialNumber currentSerialNumber
) {
}
