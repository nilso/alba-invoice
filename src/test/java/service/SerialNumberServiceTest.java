package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

	@InjectMocks
	SerialNumberService SerialNumberService;
	@Mock
	private SupplierInvoiceFacade supplierInvoiceFacade;

	@Test
	void testGetNewSerialNumberWithInvoices() throws Exception {
		SupplierId supplierId = new SupplierId("1");
		Supplier supplier = new Supplier(supplierId,
				"SE",
				"Nils AB",
				"Klientens namn",
				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
				new Address("Gatan", "", "12346", "Staden", "Landet"),
				"1234567890");
		List<Supplier> suppliers = List.of(supplier);
		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 2);

		SupplierInvoiceResponse invoice = new SupplierInvoiceResponse(supplierId, "alba01-01");
		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices = new HashMap<>();
		supplierInvoices.put(supplier.id(), List.of(invoice));

		when(supplierInvoiceFacade.fetchSerialNumberOneYearBack()).thenReturn(supplierInvoices);

		Map<SupplierId, SerialNumber> result = SerialNumberService.getNewSerialNumber(suppliers);
		assertEquals(expectedSerialNumber, result.get(supplier.id()));
	}

//	@Test
//	void testGetNewSerialNumberWithoutInvoices() throws Exception {
//		SupplierId supplierId = new SupplierId("1");
//		Supplier supplier = new Supplier(supplierId,
//				"SE",
//				"Nils AB",
//				"Klientens namn",
//				new PaymentMethod("Bankgiro", "12455", Optional.empty(), Optional.empty()),
//				new Address("Gatan", "", "12346", "Staden", "Landet"),
//				"VAT1234567890");
//		List<Supplier> suppliers = List.of(supplier);
//		SerialNumber expectedSerialNumber = new SerialNumber("alba02", 0);
//
//		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices = new HashMap<>();
//		supplierInvoices.put(new SupplierId("2"), List.of(new SupplierInvoiceResponse(new SupplierId("2"), "alba01-01")));
//
//		when(supplierInvoiceFacade.fetchSerialNumberOneYearBack()).thenReturn(supplierInvoices);
//
//		Map<SupplierId, SerialNumber> result = SerialNumberService.getNewSerialNumber(suppliers);
//		assertEquals(expectedSerialNumber, result.get(supplier.id()));
//	}

	@Test
	void testIncrementSerialNumber() {
		SupplierId supplierId = new SupplierId("1");
		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(supplierId, "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(supplierId, "alba01-02");
		List<SupplierInvoiceResponse> supplierInvoiceResponses = List.of(invoice1, invoice2);
		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 3);

		SerialNumber result = service.SerialNumberService.incrementSerialNumber(supplierInvoiceResponses);
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testIncrementSerialNumberWithEmptyList() {
		List<SupplierInvoiceResponse> supplierInvoiceResponses = Collections.emptyList();
		assertThrows(IllegalArgumentException.class, () -> service.SerialNumberService.incrementSerialNumber(supplierInvoiceResponses));
	}

	@Test
	void testFindAndIncrementHighestSerialNumber() {
		SupplierId supplierId = new SupplierId("1");
		SupplierInvoiceResponse invoice1 = new SupplierInvoiceResponse(supplierId, "alba01-01");
		SupplierInvoiceResponse invoice2 = new SupplierInvoiceResponse(supplierId, "alba02-01");
		Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices = new HashMap<>();
		supplierInvoices.put(new SupplierId("1"), List.of(invoice1, invoice2));
		SerialNumber expectedSerialNumber = new SerialNumber("alba03", 0);

		SerialNumber result = service.SerialNumberService.findAndIncrementHighestSerialNumber(supplierInvoices);
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testExtractSerialNumber() {
		String input = "alba01-01";
		SerialNumber expectedSerialNumber = new SerialNumber("alba01", 1);

		SerialNumber result = service.SerialNumberService.extractSerialNumber(input);
		assertEquals(expectedSerialNumber, result);
	}

	@Test
	void testExtractSerialNumberWithInvalidInput() {
		String input = "invalidInput";
		assertThrows(IllegalArgumentException.class, () -> service.SerialNumberService.extractSerialNumber(input));
	}
}
