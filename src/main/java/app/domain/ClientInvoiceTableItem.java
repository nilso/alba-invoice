package app.domain;

import lombok.Setter;

public final class ClientInvoiceTableItem {
	private final String id;
	private final String invoiceNr;
	private final double grossPrice;
	private final String clientName;
	@Setter
	private String commissionRate;
	@Setter
	private String lastSerialNumber;

	public ClientInvoiceTableItem(
			String id,
			String clientName,
			String invoiceNr,
			double grossPrice,
			String commissionRate,
			String lastSerialNumber
	) {
		this.id = id;
		this.clientName = clientName;
		this.invoiceNr = invoiceNr;
		this.grossPrice = grossPrice;
		this.commissionRate = commissionRate;
		this.lastSerialNumber = lastSerialNumber;
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

	@Override
	public String toString() {
		return "ClientInvoiceTableItem{" +
				"id='" + id + '\'' +
				", invoiceNr='" + invoiceNr + '\'' +
				", grossPrice=" + grossPrice +
				", clientName='" + clientName + '\'' +
				", commissionRate='" + commissionRate + '\'' +
				", lastSerialNumber='" + lastSerialNumber + '\'' +
				'}';
	}
}