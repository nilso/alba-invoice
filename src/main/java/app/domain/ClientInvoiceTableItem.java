package app.domain;

import lombok.Setter;

public final class ClientInvoiceTableItem {
	private final String id;
	private final String invoiceNr;
	private final double grossPrice;
	private final double commissionRate;
	@Setter
	private String clientName;

	public ClientInvoiceTableItem(
			String id,
			String clientName,
			String invoiceNr,
			double grossPrice,
			double commissionRate
	) {
		this.id = id;
		this.clientName = clientName;
		this.invoiceNr = invoiceNr;
		this.grossPrice = grossPrice;
		this.commissionRate = commissionRate;
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

	public double commissionRate() {
		return commissionRate;
	}

	@Override
	public String toString() {
		return "ClientInvoiceTableItem{" +
				"id='" + id + '\'' +
				", invoiceNr='" + invoiceNr + '\'' +
				", grossPrice=" + grossPrice +
				", commissionRate=" + commissionRate +
				", clientName='" + clientName + '\'' +
				'}';
	}
}