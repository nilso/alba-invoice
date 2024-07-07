package domain;

import java.util.Optional;

public final class SupplierInvoice {
	private final InvoiceId id;
	private final SupplierId supplierId;
	private final String serialNumber;
	private final InvoiceId clientInvoiceReference;

	public SupplierInvoice(InvoiceId id, SupplierId supplierId, String serialNumber, InvoiceId clientInvoiceReference) {
		this.id = id;
		this.supplierId = supplierId;
		this.serialNumber = serialNumber;
		this.clientInvoiceReference = clientInvoiceReference;
	}

	public SupplierId supplierId() {
		return supplierId;
	}

	public String serialNumber() {
		return serialNumber;
	}

	public InvoiceId id() {
		return id;
	}

	public Optional<InvoiceId> clientInvoiceReference() {
		return Optional.ofNullable(clientInvoiceReference);
	}
}
