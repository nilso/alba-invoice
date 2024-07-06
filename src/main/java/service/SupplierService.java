package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.BankAccountId;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.PaymentMethod;
import domain.Supplier;
import domain.SupplierId;
import domain.SupplierResponse;
import exception.GetSupplierException;
import facade.SupplierFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierService {
	private final SupplierFacade supplierFacade;

	public SupplierService(SupplierFacade supplierFacade) {
		this.supplierFacade = supplierFacade;
	}

	public Map<InvoiceId, Supplier> getSupplierMap(List<ClientInvoice> clientInvoices) {
		Map<InvoiceId, Supplier> supplierMap = new HashMap<>();

		clientInvoices.forEach(invoice -> {
			if (invoice.supplierId().isEmpty()) {
				return;
			}

			try {
				SupplierResponse supplierResponse = supplierFacade.fetchSupplier(invoice.supplierId().get());
				supplierMap.put(invoice.id(), mapSupplier(supplierResponse));
			} catch (Exception e) {
				log.error("Failed to fetch supplier for invoice: {}", invoice, e);
			}
		});

		return supplierMap;
	}

	private Supplier mapSupplier(SupplierResponse resp) {
		log.info("Mapping supplier: {}", resp);
		PaymentMethod paymentMethod = findPaymentMethod(resp);
		Optional<BankAccountId> bankAccountId;
		if (resp.bankAccount() != null) {
			bankAccountId = Optional.of(resp.bankAccount().bankAccountId());
		} else {
			bankAccountId = Optional.empty();
		}

		return new Supplier(resp.id(),
				resp.countryCode(),
				resp.name(),
				resp.ourReference(),
				paymentMethod,
				resp.address(),
				resp.vatNr(),
				bankAccountId);
	}

	private PaymentMethod findPaymentMethod(SupplierResponse resp) {
		if (resp.bankgiro() != null && !resp.bankgiro().equals("0")) {
			return new PaymentMethod("Bankgiro", addDashToBankgiro(resp.bankgiro()), Optional.empty(), Optional.empty());
		} else if (resp.plusgiro() != null && !resp.plusgiro().equals("0")) {
			return new PaymentMethod("Plusgiro", addDashToPlusgiro(resp.plusgiro()), Optional.empty(), Optional.empty());
		} else if (resp.swedishBankAccount() != null && !resp.swedishBankAccount().isEmpty()) {
			return new PaymentMethod("Bankkonto", resp.swedishBankAccount(), Optional.empty(), Optional.empty());
		} else if (resp.iban() != null && !resp.iban().isEmpty()) {
			return createIban(resp);
		} else if (resp.internationalBankAccountWithRouting() != null && !resp.internationalBankAccountWithRouting().isEmpty()) {
			return new PaymentMethod("Bankkonto", resp.internationalBankAccountWithRouting(), Optional.empty(), Optional.empty());
		} else if (resp.internationalBankAccountWithAccountNumber() != null && !resp.internationalBankAccountWithAccountNumber().isEmpty()) {
			return createInternationalBankAccountWithAccountNumber(resp);
		} else {
			log.info("No payment method found for supplier: {}", resp);
			return new PaymentMethod("", "", Optional.empty(), Optional.empty());
		}
	}

	private static String addDashToBankgiro(String bankgiroNumber) {
		StringBuilder sb = new StringBuilder(bankgiroNumber);
		sb.insert(bankgiroNumber.length() - 4, '-');
		return sb.toString();
	}

	private static String addDashToPlusgiro(String plusgiroNumber) {
		StringBuilder sb = new StringBuilder(plusgiroNumber);
		sb.insert(plusgiroNumber.length() - 1, '-');
		return sb.toString();
	}

	public PaymentMethod createIban(SupplierResponse supplierResponse) {
		String iban = getIban(supplierResponse.iban());
		String bic = getBic(supplierResponse.iban());
		return new PaymentMethod("BIC", bic, Optional.of("IBAN"), Optional.of(iban));
	}

	public PaymentMethod createInternationalBankAccountWithAccountNumber(SupplierResponse supplierResponse) {
		String iban = getIban(supplierResponse.internationalBankAccountWithAccountNumber());
		String bic = getBic(supplierResponse.internationalBankAccountWithAccountNumber());
		return new PaymentMethod("BIC", bic, Optional.of("IBAN"), Optional.of(iban));
	}

	private static String getIban(String iban) {
		try {
			String[] parts = iban.split("-");
			return parts[1];
		} catch (Exception e) {
			log.error("Failed to get IBAN: ", e);
			return "";
		}
	}

	private static String getBic(String iban) {
		try {
			String[] parts = iban.split("-");
			return parts[0];
		} catch (Exception e) {
			log.error("Failed to get BIC: ", e);
			return "";
		}
	}

	public List<Supplier> getAllSuppliers() throws Exception {
		return supplierFacade.fetchAllSuppliers().stream()
				.map(this::mapSupplier)
				.toList();
	}

	public Supplier getSupplier(SupplierId supplierId) throws GetSupplierException {
		try {
			SupplierResponse supplierResponse = supplierFacade.fetchSupplier(supplierId);
			return mapSupplier(supplierResponse);
		} catch (Exception e) {
			log.error("Failed to fetch supplier for supplierId: {}", supplierId, e);
			throw new GetSupplierException(e.getMessage());
		}
	}
}
