package facade;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import config.Config;
import domain.InvoiceAmounts;
import domain.SupplierInvoiceData;
import domain.SupplierInvoiceRequest;
import domain.SupplierInvoiceRequest.DepositAccount;
import domain.SupplierInvoiceResponse;
import domain.SupplierInvoicesResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierInvoiceFacade {
	private final PEHttpClient peHttpClient;
	private final ObjectMapper objectMapper;

	public SupplierInvoiceFacade(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
		this.objectMapper = new ObjectMapper();
	}

	public List<SupplierInvoiceResponse> fetchInvoicesOneYearBack() throws Exception {
		LocalDate oneYearAgo = getOneYearBack();
		log.info("Fetching supplier invoices one year back: {}", oneYearAgo);
				String endpoint = String.format("/company/%s/supplier/invoice?offset=0&limit=1000&startInvoiceDate=%s", Config.getClientId(), oneYearAgo);

		String body = peHttpClient.httpGet(endpoint);

		SupplierInvoicesResponse supplierInvoicesResponse = objectMapper.readValue(body, SupplierInvoicesResponse.class);
		if (supplierInvoicesResponse.size() == 0) {
			log.info("No supplier invoices found one year back: {}", oneYearAgo);
			return List.of();
		}

		log.info("Fetched {} supplier invoices for the financial year starting from: {}", supplierInvoicesResponse.size(), oneYearAgo);
		return supplierInvoicesResponse.supplierInvoiceResponses();
	}

	public static LocalDate getOneYearBack() {
		LocalDate today = LocalDate.now();
		return today.minusYears(1);
	}

	public List<SupplierInvoiceResponse> fetchInvoiceById(String id) throws Exception {
		String endpoint = String.format("/company/%s/supplier/invoice/%s", Config.getClientId(), id);

		String body = peHttpClient.httpGet(endpoint);

		SupplierInvoicesResponse supplierInvoicesResponse = objectMapper.readValue(body, SupplierInvoicesResponse.class);
		if (supplierInvoicesResponse.size() == 0) {
			log.info("No supplier invoices found for id: {}", id);
			return List.of();
		}

		log.info("Fetched supplierInvoice: {} for id: {}", supplierInvoicesResponse, id);
		return supplierInvoicesResponse.supplierInvoiceResponses();
	}

	public void sendInvoiceToPE(SupplierInvoiceData supplierInvoiceData, SupplierInvoiceRequest.File file) throws Exception {
		objectMapper.registerModule(new JavaTimeModule());

		String endpoint = String.format("/company/%s/supplier/invoice", Config.getClientId());
		String request = objectMapper.writeValueAsString(mapSupplierInvoiceRequest(supplierInvoiceData, file));
		log.info("Sending supplier invoice to PE: {}", supplierInvoiceData);
		peHttpClient.httpPut(endpoint,
				request);
	}

	private SupplierInvoiceRequest mapSupplierInvoiceRequest(SupplierInvoiceData supplierInvoiceData, SupplierInvoiceRequest.File file) {
		InvoiceAmounts invoiceAmounts = supplierInvoiceData.invoiceAmounts();
		DepositAccount depositAccount = mapDepositAccount(supplierInvoiceData);
		return new SupplierInvoiceRequest(supplierInvoiceData.serialNumber(),
				new SupplierInvoiceRequest.Id(Integer.parseInt(supplierInvoiceData.supplierInfo().id().getId())),
				new SupplierInvoiceRequest.Id(92446), //TODO
				supplierInvoiceData.agent().name(),
				depositAccount,
				supplierInvoiceData.invoiceDate(),
				supplierInvoiceData.dueDate(),
				supplierInvoiceData.dueDate(), //TODO paymentdate needed?!
				bigDecimalToBigInteger(supplierInvoiceData.amountDue()),
				bigDecimalToBigInteger(invoiceAmounts.vatAmount()),
				invoiceAmounts.currency(),
				new BigInteger("1"), //TODO exchange rate needed?!
				supplierInvoiceData.serialNumber(),
				"", //TODO po-nr needed?!
				"", //TODO ocr needed?!
				mapAccountingAccounts(supplierInvoiceData),
				new SupplierInvoiceRequest.Files(List.of(file)));
	}

	private DepositAccount mapDepositAccount(SupplierInvoiceData supplierInvoiceData) {
		//TODO need to handle foreign accounts
		return switch (supplierInvoiceData.paymentMethod().name()) {
			case "Bankkonto" -> new DepositAccount("BANK_ACCOUNT", supplierInvoiceData.paymentMethod().number());
			case "Plusgiro" -> new DepositAccount("PLUS_GIRO", supplierInvoiceData.paymentMethod().number());
			case "Bankgiro" -> new DepositAccount("BANK_GIRO", supplierInvoiceData.paymentMethod().number());
			default -> new DepositAccount(supplierInvoiceData.paymentMethod().name(), supplierInvoiceData.paymentMethod().number());
		};
	}

	private BigInteger bigDecimalToBigInteger(BigDecimal bigDecimal) {
		return bigDecimal.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toBigInteger();
	}

	private SupplierInvoiceRequest.Accounts mapAccountingAccounts(SupplierInvoiceData supplierInvoiceData) {
		InvoiceAmounts invoiceAmounts = supplierInvoiceData.invoiceAmounts();
		BigDecimal netPrice = supplierInvoiceData.amountDue().subtract(invoiceAmounts.vatAmount());
		return new SupplierInvoiceRequest.Accounts(List.of(new SupplierInvoiceRequest.AccountingAccount(2440, bigDecimalToBigInteger(supplierInvoiceData.amountDue()).negate()),
				new SupplierInvoiceRequest.AccountingAccount(2641, bigDecimalToBigInteger(invoiceAmounts.vatAmount())),
				new SupplierInvoiceRequest.AccountingAccount(5410, bigDecimalToBigInteger(netPrice))));
	}
}
