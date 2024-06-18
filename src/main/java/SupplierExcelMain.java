import java.util.Arrays;
import java.util.List;

import domain.Supplier;
import util.SupplierExcel;
import facade.PEHttpClient;
import facade.SupplierFacade;
import lombok.extern.slf4j.Slf4j;
import service.SupplierService;

@Slf4j
public class SupplierExcelMain {
	public static void main(String[] args) {
		//Util
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierExcel excel = new SupplierExcel();

		//Facade
		SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);

		//Service
		SupplierService supplierService = new SupplierService(supplierFacade);

		try {
			List<Supplier> allSupplier = supplierService.getAllSupplier();
			excel.createExcelFile(allSupplier);

		} catch (Exception e) {
			log.error("Failed to create supplier excel: {}", Arrays.toString(e.getStackTrace()));
		}
	}
}