package domain;

import java.util.List;

public class SuppliersResponse {
	private List<SupplierResponse> suppliers;

	// getters and setters
	public List<SupplierResponse> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<SupplierResponse> suppliers) {
		this.suppliers = suppliers;
	}
}