package service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberService {
	//TODO behöver hålla koll på att t.ex. Speranza ska ha samma löpnr serie för alla fakturor. Med samma namn?
	private static int nrOfAlreadyIncrementedPrefix;

	private final SupplierInvoiceFacade supplierInvoiceFacade;

	public SerialNumberService(SupplierInvoiceFacade supplierInvoiceFacade) {
		this.supplierInvoiceFacade = supplierInvoiceFacade;
		nrOfAlreadyIncrementedPrefix = 0;
	}

	public Map<SupplierId, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers) {
		Map<SupplierId, SerialNumber> serialNumberMap = new HashMap<>();

		suppliers.forEach(supplier -> {
			try {
				SerialNumber serialNumber;
				Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices = supplierInvoiceFacade.fetchInvoicesOneYearBack();
				if (supplierInvoices.containsKey(supplier.id())) {
					List<SupplierInvoiceResponse> supplierInvoiceResponse = supplierInvoices.get(supplier.id());
					serialNumber = getHighestSerialNumber(supplierInvoiceResponse);
				} else {
					log.info("No supplier invoices found for supplier with ID: {}", supplier.id());
					serialNumber = createNewSerial(supplierInvoices);
				}

				serialNumberMap.put(supplier.id(), serialNumber);
			} catch (Exception e) {
				log.error("Failed to fetch supplier invoices: ", e);
			}
		});

		log.info("New serial numbers: {}", serialNumberMap);
		return serialNumberMap;
	}

	private static SerialNumber getHighestSerialNumber(List<SupplierInvoiceResponse> supplierInvoiceResponses) {
		List<SerialNumber> serialNumbers = supplierInvoiceResponses.stream()
				.map(SupplierInvoiceResponse::serialNumber)
				.map(SerialNumberService::extractSerialNumber)
				.toList();

		return serialNumbers.stream()
				.max(Comparator.comparingInt(SerialNumber::suffix))
				.map(sn -> new SerialNumber(sn.prefix(), sn.suffix()))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find SerialNumber with the largest suffix"));
	}

	private static SerialNumber createNewSerial(Map<SupplierId, List<SupplierInvoiceResponse>> supplierInvoices) {
		if (supplierInvoices.isEmpty()) {
			int newPrefixNumber = 1 + nrOfAlreadyIncrementedPrefix;
			SerialNumber serialNumber = new SerialNumber("alba" + newPrefixNumber, 1);
			nrOfAlreadyIncrementedPrefix++;
			return serialNumber;
		}

		List<SerialNumber> serialNumbers = supplierInvoices.values().stream()
				.map(invoice -> extractSerialNumber(invoice.getFirst().serialNumber()))
				.sorted((sn1, sn2) -> {
					Integer num2 = extractAlbaNumber(sn2.prefix());
					Integer num1 = extractAlbaNumber(sn1.prefix());
					return num2.compareTo(num1);
				})
				.toList();

		SerialNumber highestAlba = serialNumbers.getFirst();

		nrOfAlreadyIncrementedPrefix++;
		int highestNumber = extractAlbaNumber(highestAlba.prefix());
		String newPrefix = String.format("alba%02d", highestNumber + nrOfAlreadyIncrementedPrefix);
		return new SerialNumber(newPrefix, 1);
	}

	private static SerialNumber extractSerialNumber(String input) {
		if (input == null || !input.contains("-")) {
			throw new IllegalArgumentException("Input must contain a hyphen (-)");
		}
		String[] parts = input.split("-");
		String prefix = parts[0];
		Integer suffix = Integer.parseInt(parts[1]);
		return new SerialNumber(prefix, suffix);
	}

	private static int extractAlbaNumber(String prefix) {
		return Integer.parseInt(prefix.replace("alba", ""));
	}
}
