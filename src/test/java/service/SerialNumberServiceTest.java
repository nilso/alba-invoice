package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import domain.PaymentMethod;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import domain.SupplierNameKey;
import facade.SupplierInvoiceFacade;

@ExtendWith(MockitoExtension.class)
class SerialNumberServiceTest {

	private SerialNumberService serialNumberService;

	@Mock
	private SupplierInvoiceFacade supplierInvoiceFacade;

	@Mock
	private SupplierService supplierService;

	@BeforeEach
	void setUp() {
		serialNumberService = new SerialNumberService(supplierInvoiceFacade, supplierService);
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
				"VAT1234567890");
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(List.of(invoice1, invoice2));
		when(supplierService.getAllSuppliers()).thenReturn(suppliers);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);
		SerialNumber expectedSerialNumber2 = new SerialNumber("alba02", 2);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber, result.get(new SupplierNameKey(supplier.name())));
		assertEquals(expectedSerialNumber2, result.get(new SupplierNameKey(supplier2.name())));
	}

	@Test
	void testGetNewSerialNumber_newSupplier() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId3 = new SupplierId("3");
		Supplier supplier3 = new Supplier(supplierId3,
				"SE",
				"Nils AB 3",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2, supplier3);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(List.of(invoice1, invoice2));
		when(supplierService.getAllSuppliers()).thenReturn(suppliers);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);
		SerialNumber expectedSerialNumber2 = new SerialNumber("alba02", 2);
		SerialNumber expectedSerialNumber3 = new SerialNumber("alba03", 0);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber, result.get(new SupplierNameKey(supplier.name())));
		assertEquals(expectedSerialNumber2, result.get(new SupplierNameKey(supplier2.name())));
		assertEquals(expectedSerialNumber3, result.get(new SupplierNameKey(supplier3.name())));
	}

	@Test
	void testGetNewSerialNumber_noCurrentInvoices() throws Exception {
		SupplierId supplierId3 = new SupplierId("3");
		Supplier supplier3 = new Supplier(supplierId3,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		List<Supplier> suppliers = List.of(supplier3);

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(List.of());

		SerialNumber expectedSerialNumber3 = new SerialNumber("alba1", 0);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber3, result.get(new SupplierNameKey(supplier3.name())));
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
				"VAT1234567890");
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(List.of(supplier, supplier2));
		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(List.of(invoice1, invoice2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba02", 2);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
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
				"VAT1234567890");
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(suppliers);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);

		SerialNumber result = serialNumberService.getCurrentSerialOrNewIfNone(supplier, List.of(invoice1, invoice2));
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testGetNewSerialNumber_oneSupplier_newSupplier() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB 2",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId3 = new SupplierId("3");
		Supplier supplier3 = new Supplier(supplierId3,
				"SE",
				"Nils AB 3",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2, supplier3);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(suppliers);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);

		SerialNumber result = serialNumberService.getCurrentSerialOrNewIfNone(supplier, List.of(invoice1, invoice2));
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testGetNewSerialNumber_oneSupplier_noCurrentInvoices() throws Exception {
		SupplierId supplierId3 = new SupplierId("3");
		Supplier supplier3 = new Supplier(supplierId3,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SerialNumber expectedSerialNumber3 = new SerialNumber("alba1", 0);

		SerialNumber result = serialNumberService.getCurrentSerialOrNewIfNone(supplier3, List.of());
		assertEquals(expectedSerialNumber3, result);
	}

	@Test
	void testGetNewSerialNumber_oneSupplier_noCurrentInvoicesDoesntRaiseEveryCall() throws Exception {
		SupplierId supplierId = new SupplierId("3");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId2 = new SupplierId("2");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");
		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(List.of(invoice2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba03", 0);

		Map<SupplierNameKey, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(List.of(supplier));
		assertEquals(expectedSerialNumber, result.get(new SupplierNameKey(supplier.name())));
		SerialNumber result2 = serialNumberService.getCurrentSerialOrNewIfNone(supplier, List.of(invoice2));
		assertEquals(expectedSerialNumber, result2);
		SerialNumber result3 = serialNumberService.getCurrentSerialOrNewIfNone(supplier, List.of(invoice2));
		assertEquals(expectedSerialNumber, result3);
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
				"VAT1234567890");
		SupplierId supplierId2 = new SupplierId("2");
		Supplier supplier2 = new Supplier(supplierId2,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId), "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(new SupplierInvoiceResponse.SupplierRef(supplierId2), "alba02-02");

		when(supplierService.getAllSuppliers()).thenReturn(List.of(supplier, supplier2));

		SerialNumber expectedSerialNumber = new SerialNumber("alba02", 2);

		SerialNumber result = serialNumberService.getCurrentSerialOrNewIfNone(supplier, List.of(invoice1, invoice2));
		assertEquals(expectedSerialNumber, result);
	}
}
