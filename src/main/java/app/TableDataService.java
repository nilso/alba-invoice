package app;

import static util.BigDecimalUtil.bigDecimalToPercent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.domain.ClientInvoiceTableItem;
import config.Config;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import domain.SupplierNameKey;
import domain.TableData;
import domain.User;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;
import service.ClientInvoiceService;
import service.SerialNumberService;
import service.SupplierService;
import service.UserService;

@Slf4j
public class TableDataService {

	private final ClientInvoiceService clientInvoiceService;
	private final UserService userService;
	private final SupplierService supplierService;
	private final SerialNumberService serialNumberService;
	private final SupplierInvoiceFacade supplierInvoiceFacade;
	Map<InvoiceId, User> usersByInvoiceId;
	Map<InvoiceId, Supplier> suppliersByInvoiceId;
	Map<SupplierNameKey, SerialNumber> currentSerialNumbersBySupplierNameKey;
	List<ClientInvoice> clientInvoices;
	List<SupplierInvoiceResponse> supplierInvoiceResponses;
	int daysBack;

	public TableDataService(ClientInvoiceService clientInvoiceService,
			UserService userService,
			SupplierService supplierService,
			SerialNumberService serialNumberService,
			SupplierInvoiceFacade supplierInvoiceFacade) {

		this.clientInvoiceService = clientInvoiceService;
		this.userService = userService;
		this.supplierService = supplierService;
		this.serialNumberService = serialNumberService;
		this.supplierInvoiceFacade = supplierInvoiceFacade;
		daysBack = Config.getDefaultDaysBack();
	}

	public void addCommissionRate(ClientInvoiceTableItem item, BigDecimal commissionRate) {
		ClientInvoice clientInvoice = clientInvoices.stream()
				.filter(invoice -> invoice.id().equals(new InvoiceId(invoice.invoiceNr())))
				.findFirst()
				.orElseThrow();

		clientInvoices.remove(clientInvoice);
		clientInvoice.withUITableData(commissionRate);
		clientInvoices.add(clientInvoice);
		item.setCommissionRate(bigDecimalToPercent(commissionRate));
	}

	public void addSupplier(InvoiceId invoiceId, SupplierId supplierId, ClientInvoiceTableItem item) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		suppliersByInvoiceId.put(invoiceId, supplier);
		SerialNumber serialNumber = serialNumberService.getCurrentSerialOrNewIfNone(supplier, supplierInvoiceResponses);
		currentSerialNumbersBySupplierNameKey.put(new SupplierNameKey(supplier.name()), serialNumber);
		item.setSupplier(supplier);
		item.setLastSerialNumber(serialNumber.fullSerialNumber());
	}

	public void changeDaysBack(int daysBack) throws Exception {
		this.daysBack = daysBack;
		init();
	}

	public void init() throws Exception {
		clientInvoices = clientInvoiceService.getUnprocessedClientInvoices(daysBack);
		usersByInvoiceId = userService.getUserMap(clientInvoices);
		suppliersByInvoiceId = supplierService.getSupplierMap(clientInvoices);
		supplierInvoiceResponses = supplierInvoiceFacade.fetchInvoicesOneYearBack();
		currentSerialNumbersBySupplierNameKey = serialNumberService.getCurrentSerialOrNewIfNone(suppliersByInvoiceId.values().stream().toList(),
				supplierInvoiceResponses);

	}

	public Map<InvoiceId, TableData> fetchUIData() {
		Map<InvoiceId, TableData> uiData = new HashMap<>();

		for (ClientInvoice clientInvoice : clientInvoices) {
			Optional<Supplier> supplier = Optional.empty();
			Optional<SerialNumber> currentSerialNumber = Optional.empty();
			if (suppliersByInvoiceId.containsKey(clientInvoice.id())) {
				supplier = Optional.of(suppliersByInvoiceId.get(clientInvoice.id()));
				currentSerialNumber = Optional.of(currentSerialNumbersBySupplierNameKey.get(new SupplierNameKey(supplier.get().name())));
			}
			TableData data = new TableData(
					clientInvoice,
					usersByInvoiceId.get(clientInvoice.id()),
					supplier,
					currentSerialNumber
			);
			uiData.put(clientInvoice.id(), data);
		}

		return uiData;
	}
}
