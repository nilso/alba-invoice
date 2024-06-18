package domain;

import lombok.Data;

@Data
public class InvoiceId {
	private String id;

	// Default constructor for deserialization
	public InvoiceId() {
	}

	// Constructor to handle integer values
	public InvoiceId(int id) {
		this.id = String.valueOf(id);
	}

	// Constructor to handle string values
	public InvoiceId(String id) {
		this.id = id;
	}

}
