package util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import domain.Supplier;
import facade.PEHttpClient;
import facade.SupplierFacade;
import service.SupplierService;

class SupplierExcelTest {

	@Disabled
	@Test
	void createWithRealData() {
		//Util
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierExcel excel = new SupplierExcel();

		//Facade
		SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);

		//Service
		SupplierService supplierService = new SupplierService(supplierFacade);

		try {
			List<Supplier> allSupplier = supplierService.getAllSuppliers();
			excel.createExcelFile(allSupplier);

		} catch (Exception e) {
			fail("Failed to create excel file: " + e);
		}
	}

}