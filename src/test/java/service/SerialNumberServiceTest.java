package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
import facade.SupplierInvoiceFacade;

@ExtendWith(MockitoExtension.class)
class SerialNumberServiceTest {

	private SerialNumberService serialNumberService;

	@Mock
	private SupplierInvoiceFacade supplierInvoiceFacade;


	@BeforeEach
	void setUp() {
		serialNumberService = new SerialNumberService(supplierInvoiceFacade);
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
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(supplierId, "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(supplierId2, "alba02-02");
		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoiceResponses = new HashMap<>();
		supplierInvoiceResponses.put(supplier.id(), List.of(invoice1));
		supplierInvoiceResponses.put(supplier2.id(), List.of(invoice2));

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(supplierInvoiceResponses);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);
		SerialNumber expectedSerialNumber2 = new SerialNumber("alba02", 2);

		Map<SupplierId, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber, result.get(supplier.id()));
		assertEquals(expectedSerialNumber2, result.get(supplier2.id()));
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
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");

		SupplierId supplierId3 = new SupplierId("3");
		Supplier supplier3 = new Supplier(supplierId3,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"VAT1234567890");
		List<Supplier> suppliers = List.of(supplier, supplier2, supplier3);

		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(supplierId, "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(supplierId2, "alba02-02");
		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoiceResponses = new HashMap<>();
		supplierInvoiceResponses.put(supplier.id(), List.of(invoice1));
		supplierInvoiceResponses.put(supplier2.id(), List.of(invoice2));

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(supplierInvoiceResponses);

		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);
		SerialNumber expectedSerialNumber2 = new SerialNumber("alba02", 2);
		SerialNumber expectedSerialNumber3 = new SerialNumber("alba03", 1);

		Map<SupplierId, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber, result.get(supplier.id()));
		assertEquals(expectedSerialNumber2, result.get(supplier2.id()));
		assertEquals(expectedSerialNumber3, result.get(supplier3.id()));
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

		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoiceResponses = new HashMap<>();

		when(supplierInvoiceFacade.fetchInvoicesOneYearBack()).thenReturn(supplierInvoiceResponses);

		SerialNumber expectedSerialNumber3 = new SerialNumber("alba1", 1);

		Map<SupplierId, SerialNumber> result = serialNumberService.getCurrentSerialOrNewIfNone(suppliers);
		assertEquals(expectedSerialNumber3, result.get(supplier3.id()));
	}
}
