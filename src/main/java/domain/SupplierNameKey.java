package domain;

public record SupplierNameKey(String name) {
	public SupplierNameKey {
		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}

		name = name.replace(" ", "")
				.replace("-", "")
				.replace("_", "")
				.toLowerCase();
	}
}
