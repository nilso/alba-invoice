package app.domain;

import java.util.Optional;

import domain.Supplier;
import lombok.Setter;
import lombok.ToString;

@ToString
public final class ClientInvoiceTableItem {
	private final String id;
	private final String invoiceNr;
	private final double grossPrice;
	private final String clientName;
	@Setter
	private String commissionRate;
	@Setter
	private String lastSerialNumber;
	@Setter
	private Supplier supplier;

	public ClientInvoiceTableItem(
			String id,
			String clientName,
			String invoiceNr,
			double grossPrice,
			String commissionRate,
			String lastSerialNumber,
			Supplier supplier
	) {
		this.id = id;
		this.clientName = clientName;
		this.invoiceNr = invoiceNr;
		this.grossPrice = grossPrice;
		this.commissionRate = commissionRate;
		this.lastSerialNumber = lastSerialNumber;
		this.supplier = supplier;
	}

	public String id() {
		return id;
	}

	public String clientName() {
		return clientName;
	}

	public String invoiceNr() {
		return invoiceNr;
	}

	public double grossPrice() {
		return grossPrice;
	}

	public String commissionRate() {
		return commissionRate;
	}

	public String lastSerialNumber() {
		return lastSerialNumber;
	}

	public Optional<Supplier> supplier() {
		return Optional.ofNullable(supplier);
	}
}