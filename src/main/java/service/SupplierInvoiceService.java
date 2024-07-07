package service;

import static util.CommissionCalculator.calculateCommission;
import static util.InvoiceAmountCalculator.calculateSupplierInvoiceAmounts;
import static util.VatInformationTextUtil.createVatInformationText;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.ClientInvoice;
import domain.Commission;
import domain.InvoiceAmounts;
import domain.InvoiceId;
import domain.PaymentMethod;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierInvoice;
import domain.SupplierInvoiceData;
import domain.SupplierInvoiceResponse;
import domain.SupplierNameKey;
import domain.User;
import domain.VatInformationTexts;
import facade.SupplierInvoiceFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierInvoiceService {

	private static final Map<SupplierNameKey, Integer> supplierInvoiceCounter = new HashMap<>();
	private final SupplierInvoiceFacade supplierInvoiceFacade;

	public SupplierInvoiceService(SupplierInvoiceFacade supplierInvoiceFacade) {
		this.supplierInvoiceFacade = supplierInvoiceFacade;
		supplierInvoiceCounter.clear();
	}

	public SupplierInvoiceData createSupplierInvoiceData(ClientInvoice clientInvoice, SerialNumber currentSerialNumber, Supplier supplier, User user) {

		String newSerialNumber = createSerialNumber(currentSerialNumber, supplier.name());
		return mapSupplierInvoice(clientInvoice, supplier, user, newSerialNumber);
	}

	private static String createSerialNumber(SerialNumber currentSerialNumber, String supplierName) {
		int currentSupplierInvoiceCounter = supplierInvoiceCounter.getOrDefault(new SupplierNameKey(supplierName), 0);
		SerialNumber newSerialNumber = currentSerialNumber.incrementSuffix(1 + currentSupplierInvoiceCounter);
		supplierInvoiceCounter.put(new SupplierNameKey(supplierName), currentSupplierInvoiceCounter + 1);
		return newSerialNumber.prefix() + "-" + newSerialNumber.suffix();
	}

	private static SupplierInvoiceData mapSupplierInvoice(ClientInvoice clientInvoice, Supplier supplier, User user, String newSerialNumber) {
		PaymentMethod paymentMethod = supplier.paymentMethod();

		VatInformationTexts vatVatInformationTexts = createVatInformationText(clientInvoice.client(), supplier);

		log.info("Calculating commission with net amount: {}, commission rate: {}, supplier country code: {}", clientInvoice.netPrice(), clientInvoice.commissionRate(), supplier.countryCode());

		if (clientInvoice.commissionRate().isEmpty()) {
			throw new IllegalArgumentException("Commission rate is missing for client invoice: " + clientInvoice.id());
		}

		Commission commission = calculateCommission(clientInvoice.netPrice(), clientInvoice.commissionRate().get(), supplier.countryCode());

		InvoiceAmounts invoiceAmounts = calculateSupplierInvoiceAmounts(clientInvoice);

		BigDecimal amountDue = calculateAmountDue(clientInvoice, commission);

		return new SupplierInvoiceData(mapClientInfo(clientInvoice), mapSupplierInfo(supplier), clientInvoice.invoiceDate(), clientInvoice.dueDate(), invoiceAmounts, user, newSerialNumber, paymentMethod, amountDue, commission,
				vatVatInformationTexts);
	}

	private static BigDecimal calculateAmountDue(ClientInvoice clientInvoice, Commission commission) {
		return clientInvoice.grossPrice().subtract(commission.grossCommission().add(commission.commissionRoundingAmount().orElse(BigDecimal.ZERO).negate()));
	}

	private static SupplierInvoiceData.ClientInfo mapClientInfo(ClientInvoice clientInvoice) {
		return new SupplierInvoiceData.ClientInfo(clientInvoice.client().name(), clientInvoice.invoiceAddress(), clientInvoice.client().countryCode(), clientInvoice.client().orgNo(), clientInvoice.client().vatNumber(),
				clientInvoice.invoiceNr());
	}

	private static SupplierInvoiceData.SupplierInfo mapSupplierInfo(Supplier supplier) {
		return new SupplierInvoiceData.SupplierInfo(supplier.id(), supplier.name(), supplier.address(), supplier.supplierReference(), supplier.vatNr(), supplier.countryCode());
	}

	public List<SupplierInvoice> getAllSupplierInvoicesOneYearBack() throws Exception {
		List<SupplierInvoiceResponse> supplierInvoiceResponses = supplierInvoiceFacade.fetchInvoicesOneYearBack();

		//TODO clean this up.
		return supplierInvoiceResponses.stream().map(supplierInvoiceResponse -> {
			if (supplierInvoiceResponse.clientInvoiceIds() == null) {
				return new SupplierInvoice(supplierInvoiceResponse.id(), supplierInvoiceResponse.supplierRef().supplierId(), supplierInvoiceResponse.serialNumber(), null);
			}
			InvoiceId supplierInvoiceReference = supplierInvoiceResponse.clientInvoiceIds().clientInvoiceReference().stream().findFirst().orElse(null);
			return new SupplierInvoice(supplierInvoiceResponse.id(), supplierInvoiceResponse.supplierRef().supplierId(), supplierInvoiceResponse.serialNumber(), supplierInvoiceReference);
		}).toList();
	}

	public List<SupplierInvoiceData> createSupplierInvoices(List<ClientInvoice> clientInvoices, Map<SupplierNameKey, SerialNumber> currentSerialNumbers, Map<InvoiceId, Supplier> supplierMap, Map<InvoiceId, User> userMap) {
		return clientInvoices.stream().map(clientInvoice -> {
			User user = userMap.get(clientInvoice.id());
			Supplier supplier = supplierMap.get(clientInvoice.id());
			String newSerialNumber = createSerialNumber(currentSerialNumbers, supplier.name());
			return mapSupplierInvoice(clientInvoice, supplier, user, newSerialNumber);
		}).toList();
	}

	private static String createSerialNumber(Map<SupplierNameKey, SerialNumber> currentSerialNumbers, String supplierName) {
		SupplierNameKey supplierNameKey = new SupplierNameKey(supplierName);
		int currentSupplierInvoiceCounter = supplierInvoiceCounter.getOrDefault(supplierNameKey, 0);
		SerialNumber newSerialNumber = currentSerialNumbers.get(supplierNameKey).incrementSuffix(1 + currentSupplierInvoiceCounter);
		supplierInvoiceCounter.put(supplierNameKey, currentSupplierInvoiceCounter + 1);
		return newSerialNumber.prefix() + "-" + newSerialNumber.suffix();
	}
}
