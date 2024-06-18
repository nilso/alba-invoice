import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoice;
import domain.User;
import facade.ClientFacade;
import facade.ClientInvoiceFacade;
import facade.PEHttpClient;
import facade.SupplierFacade;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;
import service.ClientInvoiceService;
import service.SerialNumberService;
import service.SupplierInvoiceService;
import service.SupplierService;
import service.UserService;
import util.PdfCreator;
import util.SupplierInvoiceExcel;

@Slf4j
public class Main {
	public static void main(String[] args) {
		//Util
		PEHttpClient peHttpClient = new PEHttpClient();
		SupplierInvoiceExcel supplierInvoiceExcel = new SupplierInvoiceExcel();
		PdfCreator pdfCreator = new PdfCreator();

		//Facade
		ClientInvoiceFacade clientInvoiceFacade = new ClientInvoiceFacade(peHttpClient);
		ClientFacade clientFacade = new ClientFacade(peHttpClient);
		SupplierInvoiceFacade supplierInvoiceFacade = new SupplierInvoiceFacade(peHttpClient, new ObjectMapper());
		SupplierFacade supplierFacade = new SupplierFacade(peHttpClient);

		//Service
		UserService userService = new UserService(peHttpClient);
		SerialNumberService serialNumberService = new SerialNumberService(supplierInvoiceFacade);
		SupplierService supplierService = new SupplierService(supplierFacade);
		ClientInvoiceService clientInvoiceService = new ClientInvoiceService(clientInvoiceFacade, clientFacade);
		SupplierInvoiceService supplierInvoiceService = new SupplierInvoiceService();

		try {
			LocalDate now = LocalDate.now();
			List<ClientInvoice> clientInvoices = clientInvoiceService.getUnprocessedClientInvoices();

			Map<InvoiceId, User> userMap = userService.getUserMap(clientInvoices);
			Map<InvoiceId, Supplier> supplierMap = supplierService.getSupplierMap(clientInvoices);
			Map<SupplierId, SerialNumber> newSerialNumbers = serialNumberService.getNewSerialNumber(supplierMap.values().stream().toList());

			List<SupplierInvoice> supplierInvoices = supplierInvoiceService.createSupplierInvoices(clientInvoices,
					newSerialNumbers,
					supplierMap,
					userMap);

			supplierInvoices.forEach(supplierInvoice -> pdfCreator.createPdf(supplierInvoice, now));
			//			supplierInvoiceExcel.createExcelFile(clientInvoices, userMap, newSerialNumbers, supplierMap);

		} catch (Exception e) {
			log.error("Failed to fetch invoices", e);
		}
	}
}