package domain;

import lombok.Data;

@Data
public class BankAccountId {
	private String id;

	// Default constructor for deserialization
	public BankAccountId() {
	}

	// Constructor to handle integer values
	public BankAccountId(int id) {
		this.id = String.valueOf(id);
	}

	// Constructor to handle string values
	public BankAccountId(String id) {
		this.id = id;
	}

}
