package service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import domain.SupplierNameKey;
import exception.GetCurrentSerialException;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberService {
	private static Map<SupplierNameKey, SerialNumber> alreadyCreatedSeries;
	private static int nrOfAlreadyIncrementedPrefix;
	private final SupplierInvoiceFacade supplierInvoiceFacade;
	private final SupplierService supplierFacade;

	public SerialNumberService(SupplierInvoiceFacade supplierInvoiceFacade,
			SupplierService supplierFacade) {
		this.supplierInvoiceFacade = supplierInvoiceFacade;
		this.supplierFacade = supplierFacade;
		nrOfAlreadyIncrementedPrefix = 0;
		alreadyCreatedSeries = new HashMap<>();
	}

	public Map<SupplierNameKey, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers) throws Exception {
		try {
			List<SupplierInvoiceResponse> allSupplierInvoices = supplierInvoiceFacade.fetchInvoicesOneYearBack();
			List<Supplier> allSuppliers = supplierFacade.getAllSuppliers();

			Map<SupplierNameKey, List<SupplierId>> supplierIdsByName = mapSuppliersByName(suppliers, allSuppliers);

			Map<SupplierNameKey, List<SupplierInvoiceResponse>> supplierInvoicesByName = mapInvoicesToSuppliers(supplierIdsByName, allSupplierInvoices);

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

	public Map<SupplierNameKey, List<SupplierInvoiceResponse>> mapInvoicesToSuppliers(Map<SupplierNameKey, List<SupplierId>> supplierIdsByName,
			List<SupplierInvoiceResponse> allInvoices) {
		Map<SupplierNameKey, List<SupplierInvoiceResponse>> resultMap = new HashMap<>();

		supplierIdsByName.forEach((supplierNameKey, supplierIds) -> {
			List<SupplierInvoiceResponse> filteredInvoices = allInvoices.stream()
					.filter(invoice -> supplierIds.contains(invoice.supplierRef().supplierId()))
					.collect(Collectors.toList());

			resultMap.put(supplierNameKey, filteredInvoices);
		});

		return resultMap;
	}

	public Map<SupplierNameKey, SerialNumber> mapInvoicesToSerialNumbers(Map<SupplierNameKey, List<SupplierInvoiceResponse>> supplierInvoicesByName,
			List<SupplierInvoiceResponse> allSupplierInvoices) {
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

	private static SerialNumber createNewSerial(SupplierNameKey supplierName, List<SupplierInvoiceResponse> allSupplierInvoices) {
		if (alreadyCreatedSeries.containsKey(supplierName)) {
			return alreadyCreatedSeries.get(supplierName);
		}

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

	private static SerialNumber extractSerialNumber(String input) {
		if (input == null || !input.contains("-")) {
			throw new IllegalArgumentException(String.format("Input must contain a hyphen (-) but was %s", input));
		}
		String[] parts = input.split("-");
		String prefix = parts[0];
		Integer suffix = Integer.parseInt(parts[1]);
		return new SerialNumber(prefix, suffix);
	}

	private static int extractAlbaNumber(String prefix) {
		return Integer.parseInt(prefix.replace("alba", ""));
	}

	public Map<SupplierNameKey, SerialNumber> getCurrentSerialOrNewIfNone(List<Supplier> suppliers, List<SupplierInvoiceResponse> allSupplierInvoices) throws Exception {
		try {
			List<Supplier> allSuppliers = supplierFacade.getAllSuppliers();

			Map<SupplierNameKey, List<SupplierId>> supplierIdsByName = mapSuppliersByName(suppliers, allSuppliers);

			Map<SupplierNameKey, List<SupplierInvoiceResponse>> supplierInvoicesByName = mapInvoicesToSuppliers(supplierIdsByName, allSupplierInvoices);

			return mapInvoicesToSerialNumbers(supplierInvoicesByName, allSupplierInvoices);

		} catch (Exception e) {
			log.error("Failed to fetch supplier invoices: ", e);
			throw e;
		}
	}

	public SerialNumber getCurrentSerialOrNewIfNone(Supplier supplier, List<SupplierInvoiceResponse> allSupplierInvoices) {
		try {
			List<Supplier> allSuppliers = supplierFacade.getAllSuppliers();

			List<SupplierId> matchingSupplierId = findMatchingSupplierIdsByName(supplier, allSuppliers);

			List<SupplierInvoiceResponse> matchingInvoices = findMatchingInvoices(matchingSupplierId, allSupplierInvoices);

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

	public List<SupplierInvoiceResponse> findMatchingInvoices(List<SupplierId> supplierIds,
			List<SupplierInvoiceResponse> allInvoices) {
		return allInvoices.stream()
				.filter(invoice -> supplierIds.contains(invoice.supplierRef().supplierId()))
				.collect(Collectors.toList());

	}

	public SerialNumber mapInvoicesToSerialNumbers(SupplierNameKey supplierNameKey, List<SupplierInvoiceResponse> matchingInvoices,
			List<SupplierInvoiceResponse> allSupplierInvoices) {
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
