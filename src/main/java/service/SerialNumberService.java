package service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoice;
import domain.SupplierNameKey;
import exception.GetCurrentSerialException;
import exception.GetSupplierInvoiceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberService {
	private static Map<SupplierNameKey, SerialNumber> alreadyCreatedSeries;
	private static int nrOfAlreadyIncrementedPrefix;
	private final SupplierInvoiceService supplierInvoiceService;
	private final SupplierService supplierService;

	public SerialNumberService(SupplierInvoiceService supplierInvoiceService,
			SupplierService supplierService) {
		this.supplierInvoiceService = supplierInvoiceService;
		this.supplierService = supplierService;
		nrOfAlreadyIncrementedPrefix = 0;
		alreadyCreatedSeries = new HashMap<>();
	}

	private static SerialNumber getHighestSerialNumber(List<SupplierInvoice> supplierInvoice) {
		List<SerialNumber> serialNumbers = supplierInvoice.stream()
				.map(SupplierInvoice::serialNumber)
				.map(SerialNumberService::extractSerialNumber)
				.toList();

		return serialNumbers.stream()
				.max(Comparator.comparingInt(SerialNumber::suffix))
				.map(sn -> new SerialNumber(sn.prefix(), sn.suffix()))
				.orElseThrow(() -> new IllegalArgumentException("Unable to find SerialNumber with the largest suffix"));
	}

	private static SerialNumber extractSerialNumber(String input) {
		if (!isValidSerialNumber(input)) {
			throw new IllegalArgumentException(String.format("Input must contain a hyphen (-) but was %s", input));
		}

		String[] parts = input.split("-");
		String prefix = parts[0];
		Integer suffix = Integer.parseInt(parts[1]);
		return new SerialNumber(prefix, suffix);
	}

	private static boolean isAlbaSerial(String input) {
		return input.contains("alba");
	}

	private static boolean isValidSerialNumber(String serialNumber) {
		return serialNumber != null && serialNumber.contains("-");
	}

	private static int extractAlbaNumber(String prefix) {
		return Integer.parseInt(prefix.replace("alba", ""));
	}

	private SerialNumber createNewSerial(SupplierNameKey supplierName) {
		if (alreadyCreatedSeries.containsKey(supplierName)) {
			return alreadyCreatedSeries.get(supplierName);
		}

		try {
			List<SupplierInvoice> allSupplierInvoices = supplierInvoiceService.getAllSupplierInvoicesOneYearBack();

			allSupplierInvoices = allSupplierInvoices.stream()
					.filter(invoice -> isValidSerialNumber(invoice.serialNumber()))
					.filter(invoice -> isAlbaSerial(invoice.serialNumber()))
					.peek(invoice -> log.info("Found serial number: {} for klientId {} ", invoice.serialNumber(), invoice.supplierId()))
					.toList();

			if (allSupplierInvoices.isEmpty()) {
				int newPrefixNumber = 1 + nrOfAlreadyIncrementedPrefix;
				SerialNumber serialNumber = new SerialNumber("alba" + newPrefixNumber, 0);
				nrOfAlreadyIncrementedPrefix++;
				alreadyCreatedSeries.put(supplierName, serialNumber);
				return serialNumber;
			}

			List<SerialNumber> serialNumbers = allSupplierInvoices.stream()
					.map(invoice -> extractSerialNumber(invoice.serialNumber()))
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
			SerialNumber serialNumber = new SerialNumber(newPrefix, 0);
			alreadyCreatedSeries.put(supplierName, serialNumber);
			return serialNumber;
		} catch (Exception e) {
			throw new GetSupplierInvoiceException(e.getMessage());
		}
	}

	public Map<SupplierNameKey, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers) throws Exception {
		try {
			List<Supplier> allSuppliers = supplierService.getAllSuppliers();

			Map<SupplierNameKey, List<SupplierId>> supplierIdsByName = groupSuppliersByMatchingName(suppliers, allSuppliers);

			Map<SupplierNameKey, List<SupplierInvoice>> supplierInvoicesByName = groupInvoicesBySupplier(supplierIdsByName);

			return mapInvoicesToSerialNumbers(supplierInvoicesByName);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw e;
		}
	}

	private Map<SupplierNameKey, List<SupplierId>> groupSuppliersByMatchingName(List<Supplier> suppliers, List<Supplier> allSuppliers) {
		Map<SupplierNameKey, List<SupplierId>> resultMap = new HashMap<>();

		suppliers.forEach(supplier -> {
			SupplierNameKey key = new SupplierNameKey(supplier.name());
			List<SupplierId> matchingSupplierIds = allSuppliers.stream()
					.filter(supplier2 -> supplier2.name().equals(supplier.name()))
					.map(Supplier::id)
					.collect(Collectors.toList());

			resultMap.put(key, matchingSupplierIds);
		});

		return resultMap;
	}

	private Map<SupplierNameKey, List<SupplierInvoice>> groupInvoicesBySupplier(Map<SupplierNameKey, List<SupplierId>> supplierIdsByName) {
		Map<SupplierNameKey, List<SupplierInvoice>> resultMap = new HashMap<>();

		supplierIdsByName.forEach((supplierNameKey, supplierIds) -> {
			try {
				List<SupplierInvoice> supplierInvoices = supplierInvoiceService.getSupplierInvoicesBySupplierIds(supplierIds);
				resultMap.put(supplierNameKey, supplierInvoices);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		});

		return resultMap;
	}

	private Map<SupplierNameKey, SerialNumber> mapInvoicesToSerialNumbers(Map<SupplierNameKey, List<SupplierInvoice>> supplierInvoicesByName) {
		Map<SupplierNameKey, SerialNumber> serialNumberMap = new HashMap<>();

		supplierInvoicesByName.forEach((key, value) -> {
			SerialNumber serialNumber;
			if (value.isEmpty()) {
				log.info("No supplier invoices found for supplier with SupplierNameKey: {}", key);
				serialNumber = createNewSerial(key);
			} else {
				log.info("Found supplier invoices: {} for supplier with SupplierNameKey: {}", value, key);
				serialNumber = getHighestSerialNumber(value);
			}
			serialNumberMap.put(key, serialNumber);
		});
		log.info("New serial numbers: {}", serialNumberMap);
		return serialNumberMap;
	}

	public SerialNumber getCurrentSerialOrNewIfNone(Supplier supplier) {
		try {
			List<Supplier> allSuppliers = supplierService.getAllSuppliers();

			List<SupplierId> matchingSupplierIds = findMatchingSupplierIdsByName(supplier, allSuppliers);

			List<SupplierInvoice> supplierInvoices = supplierInvoiceService.getSupplierInvoicesBySupplierIds(matchingSupplierIds);

			return mapInvoicesToSerialNumbers(new SupplierNameKey(supplier.name()), supplierInvoices);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw new GetCurrentSerialException(e.getMessage());
		}
	}

	private List<SupplierId> findMatchingSupplierIdsByName(Supplier supplier, List<Supplier> allSuppliers) {
		return allSuppliers.stream()
				.filter(supplier2 -> supplier2.name().equals(supplier.name()))
				.map(Supplier::id)
				.collect(Collectors.toList());
	}

	private SerialNumber mapInvoicesToSerialNumbers(SupplierNameKey supplierNameKey, List<SupplierInvoice> matchingInvoices) {
		SerialNumber serialNumber;
		if (matchingInvoices.isEmpty()) {
			log.info("No supplier invoices found for supplier with SupplierNameKey: {}", supplierNameKey);
			serialNumber = createNewSerial(supplierNameKey);
		} else {
			log.info("Found supplier invoices: {} for supplier with SupplierNameKey: {}", matchingInvoices, supplierNameKey);
			serialNumber = getHighestSerialNumber(matchingInvoices);
		}
		log.info("New serial number: {} for supplierNameKey: {}", serialNumber, supplierNameKey);
		return serialNumber;

	}
}
