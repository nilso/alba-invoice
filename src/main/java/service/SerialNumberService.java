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

	private SerialNumber createNewSerial(SupplierNameKey supplierName,
			List<SupplierInvoice> allSupplierInvoices) {
		if (alreadyCreatedSeries.containsKey(supplierName)) {
			return alreadyCreatedSeries.get(supplierName);
		}

		allSupplierInvoices = allSupplierInvoices.stream()
				.filter(invoice -> isValidSerialNumber(invoice.serialNumber()))
				.filter(invoice -> isAlbaSerial(invoice.serialNumber()))
				.peek(invoice -> log.info("Found serial number: {} for klientId {} ", invoice.serialNumber(), invoice.supplierId()))
				.collect(Collectors.toList());

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
	}

	public Map<SupplierNameKey, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers) throws Exception {
		try {
			List<SupplierInvoice> allSupplierInvoices = supplierInvoiceService.getAllSupplierInvoicesOneYearBack();
			List<Supplier> allSuppliers = supplierService.getAllSuppliers();

			Map<SupplierNameKey, List<SupplierId>> supplierIdsByName = mapSuppliersByName(suppliers, allSuppliers);

			Map<SupplierNameKey, List<SupplierInvoice>> supplierInvoicesByName = mapInvoicesToSuppliers(supplierIdsByName, allSupplierInvoices);

			return mapInvoicesToSerialNumbers(supplierInvoicesByName, allSupplierInvoices);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw e;
		}
	}

	public Map<SupplierNameKey, List<SupplierId>> mapSuppliersByName(List<Supplier> suppliers, List<Supplier> allSuppliers) {
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

	public Map<SupplierNameKey, List<SupplierInvoice>> mapInvoicesToSuppliers(Map<SupplierNameKey, List<SupplierId>> supplierIdsByName,
			List<SupplierInvoice> allInvoices) {
		Map<SupplierNameKey, List<SupplierInvoice>> resultMap = new HashMap<>();

		supplierIdsByName.forEach((supplierNameKey, supplierIds) -> {
			List<SupplierInvoice> filteredInvoices = allInvoices.stream()
					.filter(invoice -> supplierIds.contains(invoice.supplierId()))
					.collect(Collectors.toList());

			resultMap.put(supplierNameKey, filteredInvoices);
		});

		return resultMap;
	}

	public Map<SupplierNameKey, SerialNumber> mapInvoicesToSerialNumbers(Map<SupplierNameKey, List<SupplierInvoice>> supplierInvoicesByName,
			List<SupplierInvoice> allSupplierInvoices) {
		Map<SupplierNameKey, SerialNumber> serialNumberMap = new HashMap<>();

		supplierInvoicesByName.forEach((key, value) -> {
			SerialNumber serialNumber;
			if (value.isEmpty()) {
				log.info("No supplier invoices found for supplier with SupplierNameKey: {}", key);
				serialNumber = createNewSerial(key, allSupplierInvoices);
			} else {
				log.info("Found supplier invoices: {} for supplier with SupplierNameKey: {}", value, key);
				serialNumber = getHighestSerialNumber(value);
			}
			serialNumberMap.put(key, serialNumber);
		});
		log.info("New serial numbers: {}", serialNumberMap);
		return serialNumberMap;
	}

	public Map<SupplierNameKey, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers, List<SupplierInvoice> allSupplierInvoices) throws Exception {
		try {
			List<Supplier> allSuppliers = supplierService.getAllSuppliers();

			Map<SupplierNameKey, List<SupplierId>> supplierIdsByName = mapSuppliersByName(suppliers, allSuppliers);

			Map<SupplierNameKey, List<SupplierInvoice>> supplierInvoicesByName = mapInvoicesToSuppliers(supplierIdsByName, allSupplierInvoices);

			return mapInvoicesToSerialNumbers(supplierInvoicesByName, allSupplierInvoices);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw e;
		}
	}

	public SerialNumber getCurrentSerialOrNewIfNone(Supplier supplier, List<SupplierInvoice> allSupplierInvoices) {
		try {
			List<Supplier> allSuppliers = supplierService.getAllSuppliers();

			List<SupplierId> matchingSupplierId = findMatchingSupplierIdsByName(supplier, allSuppliers);

			List<SupplierInvoice> matchingInvoices = findMatchingInvoices(matchingSupplierId, allSupplierInvoices);

			return mapInvoicesToSerialNumbers(new SupplierNameKey(supplier.name()), matchingInvoices, allSupplierInvoices);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw new GetCurrentSerialException(e.getMessage());
		}
	}

	public List<SupplierId> findMatchingSupplierIdsByName(Supplier supplier, List<Supplier> allSuppliers) {
		return allSuppliers.stream()
				.filter(supplier2 -> supplier2.name().equals(supplier.name()))
				.map(Supplier::id)
				.collect(Collectors.toList());
	}

	public List<SupplierInvoice> findMatchingInvoices(List<SupplierId> supplierIds,
			List<SupplierInvoice> allInvoices) {
		return allInvoices.stream()
				.filter(invoice -> supplierIds.contains(invoice.supplierId()))
				.collect(Collectors.toList());

	}

	public SerialNumber mapInvoicesToSerialNumbers(SupplierNameKey supplierNameKey, List<SupplierInvoice> matchingInvoices,
			List<SupplierInvoice> allSupplierInvoices) {
		SerialNumber serialNumber;
		if (matchingInvoices.isEmpty()) {
			log.info("No supplier invoices found for supplier with SupplierNameKey: {}", supplierNameKey);
			serialNumber = createNewSerial(supplierNameKey, allSupplierInvoices);
		} else {
			log.info("Found supplier invoices: {} for supplier with SupplierNameKey: {}", matchingInvoices, supplierNameKey);
			serialNumber = getHighestSerialNumber(matchingInvoices);
		}
		log.info("New serial number: {} for supplierNameKey: {}", serialNumber, supplierNameKey);
		return serialNumber;

	}
}
