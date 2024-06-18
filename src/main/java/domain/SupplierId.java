package domain;

import lombok.Data;

@Data
public class SupplierId {
	private String id;

	// Default constructor for deserialization
	public SupplierId() {
	}

	// Constructor to handle integer values
	public SupplierId(int id) {
		this.id = String.valueOf(id);
	}

	// Constructor to handle string values
	public SupplierId(String id) {
		this.id = id;
	}

}
