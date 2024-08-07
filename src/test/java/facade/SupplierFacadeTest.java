package facade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import domain.SupplierId;

class SupplierFacadeTest {

	@Disabled
	@Test
	void fetchAllSuppliers() {
		SupplierFacade supplierFacade = new SupplierFacade(new PEHttpClient());
		assertDoesNotThrow(supplierFacade::fetchAllSuppliers);
	}

	@Disabled
	@Test
	void fetchSupplierById() {
		SupplierFacade supplierFacade = new SupplierFacade(new PEHttpClient());
		assertDoesNotThrow(() -> supplierFacade.fetchSupplier(new SupplierId(199953)));
	}
}