package service;

import java.util.List;
import java.util.Map;

import domain.BankAccount;
import domain.BankAccountId;
import domain.Supplier;
import facade.BankAccountFacade;
import util.SupplierIdDocumentCreator;

public class SupplierIdDocumentService {
	private final SupplierService supplierService;
	private final BankAccountFacade bankAccountFacade;
	private final SupplierIdDocumentCreator excel;

	public SupplierIdDocumentService(SupplierService supplierService,
			BankAccountFacade bankAccountFacade,
			SupplierIdDocumentCreator excel) {
		this.supplierService = supplierService;
		this.bankAccountFacade = bankAccountFacade;
		this.excel = excel;
	}

	public String  createSupplierIdDocument() throws Exception {
		List<Supplier> allSupplier = supplierService.getAllSuppliers();
		Map<BankAccountId, BankAccount> bankAccountResponseById = bankAccountFacade.fetchBankAccounts();
		return excel.createExcelFile(allSupplier, bankAccountResponseById);
	}
}
