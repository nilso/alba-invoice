package app;

import static util.BigDecimalUtil.bigDecimalToPercent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import app.domain.ClientInvoiceTableItem;
import config.Config;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoice;
import domain.SupplierNameKey;
import domain.TableData;
import domain.User;
import lombok.extern.slf4j.Slf4j;
import service.ClientInvoiceService;
import service.SerialNumberService;
import service.SupplierInvoiceService;
import service.SupplierService;
import service.UserService;

@Slf4j
public class TableDataService {

	private final ClientInvoiceService clientInvoiceService;
	private final UserService userService;
	private final SupplierService supplierService;
	private final SerialNumberService serialNumberService;
	private final SupplierInvoiceService supplierInvoiceService;
	Map<InvoiceId, User> usersByInvoiceId;
	Map<InvoiceId, Supplier> suppliersByInvoiceId;
	Map<SupplierNameKey, SerialNumber> currentSerialNumbersBySupplierNameKey;
	Map<InvoiceId, ClientInvoice> clientInvoices;
	List<SupplierInvoice> supplierInvoices;
	int daysBack;

	public TableDataService(ClientInvoiceService clientInvoiceService,
			UserService userService,
			SupplierService supplierService,
			SerialNumberService serialNumberService,
			SupplierInvoiceService supplierInvoiceService) {

		this.clientInvoiceService = clientInvoiceService;
		this.userService = userService;
		this.supplierService = supplierService;
		this.serialNumberService = serialNumberService;
		this.supplierInvoiceService = supplierInvoiceService;
		daysBack = Config.getDefaultDaysBack();
	}

	public void addCommissionRate(ClientInvoiceTableItem item, BigDecimal commissionRate) {
		InvoiceId invoiceId = new InvoiceId(item.id());
		ClientInvoice clientInvoice = clientInvoices.get(invoiceId);

		if (clientInvoice != null) {
			ClientInvoice updatedInvoice = clientInvoice.withUITableData(commissionRate);
			clientInvoices.put(invoiceId, updatedInvoice);  // Update the map
			item.setCommissionRate(bigDecimalToPercent(commissionRate));
		} else {
			throw new IllegalArgumentException("Invoice with ID " + item.id() + " not found");
		}
	}

	public void addSupplier(InvoiceId invoiceId, SupplierId supplierId, ClientInvoiceTableItem item) {
		Supplier supplier = supplierService.getSupplier(supplierId);
		suppliersByInvoiceId.put(invoiceId, supplier);
		SerialNumber serialNumber = serialNumberService.getCurrentSerialOrNewIfNone(supplier, supplierInvoices);
		currentSerialNumbersBySupplierNameKey.put(new SupplierNameKey(supplier.name()), serialNumber);
		item.setSupplier(supplier);
		item.setLastSerialNumber(serialNumber.fullSerialNumber());
	}

	public void changeDaysBack(int daysBack) throws Exception {
		this.daysBack = daysBack;
		init();
	}

	public void init() throws Exception {
		clientInvoices = clientInvoiceService.getUnprocessedClientInvoices(daysBack).stream().collect(Collectors.toMap(ClientInvoice::id, c -> c));
		usersByInvoiceId = userService.getUserMap(clientInvoices.values().stream().toList());
		suppliersByInvoiceId = supplierService.getSupplierMap(clientInvoices.values().stream().toList());
		supplierInvoices = supplierInvoiceService.getAllSupplierInvoicesOneYearBack();
		currentSerialNumbersBySupplierNameKey = serialNumberService.getCurrentSerialOrNewIfNone(suppliersByInvoiceId.values().stream().toList(),
				supplierInvoices);

	}

	public Map<InvoiceId, TableData> fetchUIData() {
		Map<InvoiceId, TableData> uiData = new HashMap<>();

		for (ClientInvoice clientInvoice : clientInvoices.values()) {
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

	public String getSupplierInvoiceReference(InvoiceId clientInvoiceId) {
		return supplierInvoices.stream()
				.filter(supplierInvoice -> supplierInvoice.clientInvoiceReference().isPresent()
						&& supplierInvoice.clientInvoiceReference().get().equals(clientInvoiceId))
				.findFirst()
				.map(SupplierInvoice::serialNumber)
				.orElse("");
	}
}
