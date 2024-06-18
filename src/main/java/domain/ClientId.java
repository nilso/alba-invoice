package domain;

import lombok.Data;

@Data
public class ClientId {
	private String id;

	// Default constructor for deserialization
	public ClientId() {
	}

	// Constructor to handle integer values
	public ClientId(int id) {
		this.id = String.valueOf(id);
	}

	// Constructor to handle string values
	public ClientId(String id) {
		this.id = id;
	}

}
