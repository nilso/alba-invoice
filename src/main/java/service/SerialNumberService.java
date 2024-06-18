package service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberService {
	//TODO av någon anledning stegar den inte när jag har flera fakturor på samma leverantör som inte har löpnr sen tidigare.
	static int nrOfAlreadyIncrementedSerialNumbers = 0;

	private final SupplierInvoiceFacade supplierInvoiceFacade;

	public SerialNumberService(SupplierInvoiceFacade supplierInvoiceFacade) {
		this.supplierInvoiceFacade = supplierInvoiceFacade;
	}
	public Map<SupplierId, SerialNumber> getNewSerialNumber(List<Supplier> suppliers) {
		Map<SupplierId, SerialNumber> serialNumberMap = new HashMap<>();

		suppliers.forEach(supplier -> {
			try {
				SerialNumber newSerialNumber;
				Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices = supplierInvoiceFacade.fetchSerialNumberOneYearBack();
				if (supplierInvoices.containsKey(supplier.id())) {
					List<SupplierInvoiceResponse> supplierInvoiceResponse = supplierInvoices.get(supplier.id());
					newSerialNumber = incrementSerialNumber(supplierInvoiceResponse);
				} else {
					log.info("No supplier invoices found for supplier with ID: {}", supplier.id());
					newSerialNumber = findAndIncrementHighestSerialNumber(supplierInvoices);
				}

				serialNumberMap.put(supplier.id(), newSerialNumber);
			} catch (Exception e) {
				log.error("Failed to fetch supplier invoices: ", e);
			}
		});

		log.info("New serial numbers: {}", serialNumberMap);
		return serialNumberMap;
	}

	public static SerialNumber incrementSerialNumber(List<SupplierInvoiceResponse> supplierInvoiceResponses) {
		List<SerialNumber> serialNumbers = supplierInvoiceResponses.stream()
				.map(SupplierInvoiceResponse::serialNumber)
				.map(SerialNumberService::extractSerialNumber)
				.toList();

		return serialNumbers.stream()
				.max(Comparator.comparingInt(SerialNumber::suffix))
				.map(sn -> new SerialNumber(sn.prefix(), sn.suffix() + 1))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find SerialNumber with the largest suffix"));
	}

	public static SerialNumber findAndIncrementHighestSerialNumber(Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices) {
		List<SerialNumber> serialNumbers = supplierInvoices.values().stream()
				.flatMap(List::stream)
				.map(SupplierInvoiceResponse::serialNumber)
				.filter(serialNumber -> serialNumber.contains("alba"))
				.map(SerialNumberService::extractSerialNumber)
				.toList();

		Optional<SerialNumber> highestAlba = serialNumbers.stream()
				.filter(sn -> sn.prefix().startsWith("alba"))
				.max((sn1, sn2) -> {
					Integer num1 = extractAlbaNumber(sn1.prefix());
					Integer num2 = extractAlbaNumber(sn2.prefix());
					return num1.compareTo(num2);
				});

		nrOfAlreadyIncrementedSerialNumbers++;
		if (highestAlba.isPresent()) {
			SerialNumber highest = highestAlba.get();
			int highestNumber = extractAlbaNumber(highest.prefix());
			String newPrefix = String.format("alba%02d", highestNumber + nrOfAlreadyIncrementedSerialNumbers);
			return new SerialNumber(newPrefix, 0);
		} else {
			log.warn("No serial numbers found for the prefix 'alba'");
			return new SerialNumber("alba01", 0);
		}
	}

	public static int extractAlbaNumber(String prefix) {
		return Integer.parseInt(prefix.replace("alba",""));
	}

	public static SerialNumber extractSerialNumber(String input) {
		if (input == null || !input.contains("-")) {
			throw new IllegalArgumentException("Input must contain a hyphen (-)");
		}
		String[] parts = input.split("-");
		String prefix = parts[0];
		Integer suffix = Integer.parseInt(parts[1]);
		return new SerialNumber(prefix, suffix);
	}
}
