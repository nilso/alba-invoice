package facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.SupplierId;
import domain.SupplierInvoiceResponse;
import domain.SupplierInvoicesResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierInvoiceFacade {
	private final PEHttpClient peHttpClient;
	private final ObjectMapper objectMapper;

	public SupplierInvoiceFacade(PEHttpClient peHttpClient, ObjectMapper objectMapper) {
		this.peHttpClient = peHttpClient;
		this.objectMapper = objectMapper;
	}

	public Map<SupplierId, List<SupplierInvoiceResponse>> fetchSerialNumberOneYearBack() throws Exception {
		LocalDate oneYearAgo = getOneYearBack();
		log.info("Fetching supplier invoices one year back: {}", oneYearAgo);
		String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), oneYearAgo);

		String body = peHttpClient.httpCall(endpoint);

		SupplierInvoicesResponse supplierInvoicesResponse = objectMapper.readValue(body, SupplierInvoicesResponse.class);
		if (supplierInvoicesResponse.size() == 0) {
			log.info("No supplier invoices found one year back: {}", oneYearAgo);
			return Map.of();
		}

		log.info("Fetched {} supplier invoices for the financial year starting from: {}", supplierInvoicesResponse.size(), oneYearAgo);
		return supplierInvoicesResponse.supplierInvoiceResponses().stream()
				.collect(Collectors.groupingBy(SupplierInvoiceResponse::supplierId));
	}

	public static LocalDate getOneYearBack() {
		LocalDate today = LocalDate.now();
		return today.minusYears(1);
	}
}
