package util;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import domain.BankAccount;
import domain.BankAccountId;
import domain.Supplier;
import facade.BankAccountFacade;
import facade.PEHttpClient;
import facade.SupplierFacade;
import service.SupplierService;

class SupplierExcelTest {

	@Disabled
	@Test
	void createWithRealData() throws Exception {
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierExcel excel = new SupplierExcel();
		BankAccountFacade bankAccountFacade = new BankAccountFacade(peHttpClient);
		SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);
		SupplierService supplierService = new SupplierService(supplierFacade);

		List<Supplier> allSupplier = supplierService.getAllSuppliers();
		Map<BankAccountId, BankAccount> bankAccountResponseById = bankAccountFacade.fetchBankAccounts();
		excel.createExcelFile(allSupplier, bankAccountResponseById);
	}

}