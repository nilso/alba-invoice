package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.UIData;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierNameKey;
import domain.User;

public class UIDataService {

	private final ClientInvoiceService clientInvoiceService;
	private final UserService userService;
	private final SupplierService supplierService;
	private final SerialNumberService serialNumberService;

	public UIDataService(ClientInvoiceService clientInvoiceService,
			UserService userService,
			SupplierService supplierService,
			SerialNumberService serialNumberService) {

		this.clientInvoiceService = clientInvoiceService;
		this.userService = userService;
		this.supplierService = supplierService;
		this.serialNumberService = serialNumberService;
	}

	public Map<InvoiceId, UIData> fetchUIData(int daysBack) throws Exception {
		List<ClientInvoice> clientInvoices = clientInvoiceService.getUnprocessedClientInvoices(daysBack);
		return fetchUIData(clientInvoices);
	}

	public Map<InvoiceId, UIData> fetchUIData(List<ClientInvoice> clientInvoices) throws Exception {
		Map<InvoiceId, UIData> uiData = new HashMap<>();
		Map<InvoiceId, User> userMap = userService.getUserMap(clientInvoices);
		Map<InvoiceId, Supplier> supplierMap = supplierService.getSupplierMap(clientInvoices);
		Map<SupplierNameKey, SerialNumber> currentSerialNumbers = serialNumberService.getCurrentSerialOrNewIfNone(supplierMap.values().stream().toList());

		for (ClientInvoice clientInvoice : clientInvoices) {
			UIData data = new UIData(
					clientInvoice,
					userMap.get(clientInvoice.id()),
					supplierMap.get(clientInvoice.id()),
					currentSerialNumbers.get(new SupplierNameKey(supplierMap.get(clientInvoice.id()).name()))
			);
			uiData.put(clientInvoice.id(), data);
		}

		return uiData;
	}

	public Map<InvoiceId, UIData> fetchUIData() throws Exception {
		List<ClientInvoice> clientInvoices = clientInvoiceService.getUnprocessedClientInvoices();
		return fetchUIData(clientInvoices);
	}
}
