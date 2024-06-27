package facade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import config.Config;
import domain.SupplierId;
import domain.SupplierInvoice;
import domain.SupplierInvoiceRequest;
import domain.SupplierInvoiceRequest.DepositAccount;
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

	public Map<SupplierId, List<SupplierInvoiceResponse>> fetchInvoicesOneYearBack() throws Exception {
		LocalDate oneYearAgo = getOneYearBack();
		log.info("Fetching supplier invoices one year back: {}", oneYearAgo);
		String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), oneYearAgo);

		String body = peHttpClient.httpGet(endpoint);

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

	public void sendInvoiceToPE(SupplierInvoice supplierInvoice, SupplierInvoiceRequest.File file) throws Exception {
		objectMapper.registerModule(new JavaTimeModule());

		String endpoint = String.format("/company/%s/supplier/invoice", Config.getClientId());
		String request = objectMapper.writeValueAsString(mapSupplierInvoiceRequest(supplierInvoice, file));
		log.info("Sending supplier invoice to PE: {}", request);
		peHttpClient.httpPut(endpoint,
				request);
	}

	private SupplierInvoiceRequest mapSupplierInvoiceRequest(SupplierInvoice supplierInvoice, SupplierInvoiceRequest.File file) {

		DepositAccount depositAccount = mapDepositAccount(supplierInvoice);
		return new SupplierInvoiceRequest(supplierInvoice.serialNumber(),
				new SupplierInvoiceRequest.Id(Integer.parseInt(supplierInvoice.supplierId().getId())),
				new SupplierInvoiceRequest.Id(92446),
				supplierInvoice.agentReference(),
				depositAccount,
				supplierInvoice.clientInvoice().invoiceDate(),
				supplierInvoice.clientInvoice().dueDate(),
				supplierInvoice.clientInvoice().dueDate(), //TODO paymentdate needed?!
				bigDecimalToBigInteger(supplierInvoice.grossPrice()),
				bigDecimalToBigInteger(supplierInvoice.vatAmount()),
				supplierInvoice.clientInvoice().currency(),
				new BigInteger("1"), //TODO exchange rate needed?!
				supplierInvoice.serialNumber(),
				"", //TODO po-nr needed?!
				"", //TODO ocr needed?!
				mapAccountingAccounts(supplierInvoice),
				List.of(file));
	}

	private DepositAccount mapDepositAccount(SupplierInvoice supplierInvoice) {
		return switch (supplierInvoice.paymentMethod().name()) {
			case "Bankkonto" -> new DepositAccount("BANK_ACCOUNT", supplierInvoice.paymentMethod().number());
			case "Plusgiro" -> new DepositAccount("PLUS_GIRO", supplierInvoice.paymentMethod().number());
			case "Bankgiro" -> new DepositAccount("BANK_GIRO", supplierInvoice.paymentMethod().number());
			default -> new DepositAccount(supplierInvoice.paymentMethod().name(), supplierInvoice.paymentMethod().number());
		};
	}

	private BigInteger bigDecimalToBigInteger(BigDecimal bigDecimal) {
		return bigDecimal.setScale(2, RoundingMode.HALF_UP).toBigInteger();
	}

	private List<SupplierInvoiceRequest.AccountingAccount> mapAccountingAccounts(SupplierInvoice supplierInvoice) {
		BigDecimal netPrice = supplierInvoice.grossPrice().subtract(supplierInvoice.vatAmount());
		return List.of(new SupplierInvoiceRequest.AccountingAccount(2440, bigDecimalToBigInteger(supplierInvoice.grossPrice()).negate()),
				new SupplierInvoiceRequest.AccountingAccount(2641, bigDecimalToBigInteger(supplierInvoice.vatAmount())),
				new SupplierInvoiceRequest.AccountingAccount(5410, bigDecimalToBigInteger(netPrice)));
	}
}
