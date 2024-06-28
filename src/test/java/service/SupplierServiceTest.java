package service;

import facade.PEHttpClient;
import facade.SupplierFacade;

class SupplierServiceTest {
	PEHttpClient peHttpClient = new PEHttpClient();
	SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);
	SupplierService supplierService = new SupplierService(supplierFacade);

	//	@Test
	//	void test() {
	//		supplierService.getSupplierMap()
	//	}

}