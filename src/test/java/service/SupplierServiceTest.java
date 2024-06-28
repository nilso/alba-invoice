package service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import facade.PEHttpClient;
import facade.SupplierFacade;

class SupplierServiceTest {
	PEHttpClient peHttpClient = new PEHttpClient();
	SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);
	SupplierService supplierService = new SupplierService(supplierFacade);

	@Disabled
	@Test
	void test() {
//		supplierService.getSupplierMap()
	}

}