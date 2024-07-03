package app.domain;

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
	private String supplierName;
	@Setter
	private String supplierId;

	public ClientInvoiceTableItem(
			String id,
			String clientName,
			String invoiceNr,
			double grossPrice,
			String commissionRate,
			String lastSerialNumber,
			String supplierName,
			String supplierId
	) {
		this.id = id;
		this.clientName = clientName;
		this.invoiceNr = invoiceNr;
		this.grossPrice = grossPrice;
		this.commissionRate = commissionRate;
		this.lastSerialNumber = lastSerialNumber;
		this.supplierName = supplierName;
		this.supplierId = supplierId;
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

	public String supplierName() {
		return supplierName;
	}

	public String supplierId() {
		return supplierId;
	}
}