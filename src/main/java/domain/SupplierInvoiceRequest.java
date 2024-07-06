package domain;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupplierInvoiceRequest(
		@JsonProperty("foreign-id") String foreignId,
		@JsonProperty("supplier") Id supplierId,
		@JsonProperty("your-reference") Id yourReferenceId,
		@JsonProperty("our-reference") String ourReference,
		@JsonProperty("deposit-account") DepositAccount depositAccount,
		@JsonProperty("invoice-date") String invoiceDate,
		@JsonProperty("due-date") String dueDate,
		@JsonProperty("payment-date") String paymentDate,
		BigInteger amount,
		BigInteger vat,
		String currency,
		@JsonProperty("currency-rate") BigInteger currencyRate,
		@JsonProperty("reference-nr") String referenceNr,
		@JsonProperty("po-nr") String poNr,
		String ocr,
		Accounts accounts,
		Files files
) {

	public record Id(int id) {
	}

	public record Files(List<File> files) {
	}

	public record File(String filePath, int[] data) {
	}

	public record DepositAccount(String type, String nr) {
	}

	public record Accounts(List<AccountingAccount> accounts) {

	}

	public record AccountingAccount(@JsonProperty("account-nr") int accountNr,
									BigInteger amount) {
	}

	public record Period(@JsonProperty("start-date") LocalDate startDate, @JsonProperty("end-date") LocalDate endDate) {
	}
}
