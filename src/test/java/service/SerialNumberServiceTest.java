package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domain.Address;
import domain.BankAccountId;
import domain.InvoiceId;
import domain.PaymentMethod;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoice;
import domain.SupplierNameKey;

@ExtendWith(MockitoExtension.class)
class SerialNumberServiceTest {

	private SerialNumberService serialNumberService;

	@Mock
	private SupplierInvoiceService supplierInvoiceService;

	@Mock
	private SupplierService supplierService;

	@BeforeEach
	void setUp() {
		serialNumberService = new SerialNumberService(supplierInvoiceService, supplierService);
	}

	@Test
	void testGetNewSerialNumber_existingAlbaOrCurrentSerial() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		List<Supplier> suppliers = List.of(supplier, supplier2);

		SupplierInvoice invoice1 = aSupplierInvoiceResponse();
		SupplierInvoice invoice2 = aSupplierInvoiceResponse("2", supplierId2, "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(suppliers);
		when(supplierInvoiceService.getSupplierInvoicesBySupplierIds(List.of(supplierId))).thenReturn(List.of(invoice1));
		when(supplierInvoiceService.getSupplierInvoicesBySupplierIds(List.of(supplierId2))).thenReturn(List.of(invoice2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);
		SerialNumber expectedSerialNumber2 = new SerialNumber("alba02", 2);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialNumber(suppliers);
		assertEquals(expectedSerialNumber, result.get(new SupplierNameKey(supplier.name())));
		assertEquals(expectedSerialNumber2, result.get(new SupplierNameKey(supplier2.name())));
	}

	private SupplierInvoice aSupplierInvoiceResponse() {
		return aSupplierInvoiceResponse("1", new SupplierId("1"), "alba01-01");
	}

	private SupplierInvoice aSupplierInvoiceResponse(String id, SupplierId supplierId, String serialNumber) {
		return new SupplierInvoice(new InvoiceId(id), supplierId, serialNumber, new InvoiceId("1"));
	}

	@Test
	void testGetNewSerialNumber_serialOnDifferentSupplierWithSameName() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		List<Supplier> suppliers = List.of(supplier);

		SupplierInvoice invoice1 = aSupplierInvoiceResponse();
		SupplierInvoice invoice2 = aSupplierInvoiceResponse("2", supplierId2, "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(List.of(supplier, supplier2));
		when(supplierInvoiceService.getSupplierInvoicesBySupplierIds(any())).thenReturn(List.of(invoice1, invoice2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba02", 2);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialNumber(suppliers);
		assertEquals(expectedSerialNumber, result.get(new SupplierNameKey(supplier.name())));
	}

	@Test
	void testGetNewSerialNumber_oneSupplier_existingAlbaOrCurrentSerial() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		List<Supplier> suppliers = List.of(supplier, supplier2);

		SupplierInvoice invoice1 = aSupplierInvoiceResponse();

		when(supplierService.getAllSuppliers()).thenReturn(suppliers);
		when(supplierInvoiceService.getSupplierInvoicesBySupplierIds(any())).thenReturn(List.of(invoice1));

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);

		SerialNumber result = serialNumberService.getCurrentSerialNumber(supplier);
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testGetNewSerialNumber_oneSupplier_serialOnDifferentSupplierWithSameName() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890",
				Optional.of(new BankAccountId("1")));

		SupplierInvoice invoice1 = aSupplierInvoiceResponse();
		SupplierInvoice invoice2 = aSupplierInvoiceResponse("2", supplierId2, "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(List.of(supplier, supplier2));
		when(supplierInvoiceService.getSupplierInvoicesBySupplierIds(any())).thenReturn(List.of(invoice1, invoice2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba02", 2);

		SerialNumber result = serialNumberService.getCurrentSerialNumber(supplier);
		assertEquals(expectedSerialNumber, result);
	}
}
