import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierInvoice;
import domain.SupplierInvoiceRequest;
import domain.SupplierNameKey;
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

@Slf4j
public class SendInvoicesToPeMain {
	public static void main(String[] args) {
		//Util
		PEHttpClient peHttpClient = new PEHttpClient();
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
			Map<SupplierNameKey, SerialNumber> newSerialNumbers = serialNumberService.getCurrentSerialOrNewIfNone(supplierMap.values().stream().toList());

			List<SupplierInvoice> supplierInvoices = supplierInvoiceService.createSupplierInvoices(clientInvoices,
					newSerialNumbers,
					supplierMap,
					userMap);

			supplierInvoices.forEach(supplierInvoice -> {
				SupplierInvoiceRequest.File file = pdfCreator.createPdf(supplierInvoice, now);
				try {
					supplierInvoiceFacade.sendInvoiceToPE(supplierInvoice, file);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			log.error("Failed to fetch invoices", e);
		}
	}
}