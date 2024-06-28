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
import domain.SupplierNameKey;
import domain.User;
import domain.VatInformationTexts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierInvoiceService {

	private static final Map<SupplierNameKey, Integer> supplierInvoiceCounter = new HashMap<>();

	public SupplierInvoiceService() {
		supplierInvoiceCounter.clear();
	}

	public List<SupplierInvoice> createSupplierInvoices(List<ClientInvoice> clientInvoices,
			Map<SupplierNameKey, SerialNumber> currentSerialNumbers,
			Map<InvoiceId, Supplier> supplierMap,
			Map<InvoiceId, User> userMap) {
		return clientInvoices.stream().map(clientInvoice -> {
			Supplier supplier = supplierMap.get(clientInvoice.id());
			String newSerialNumber = createSerialNumber(currentSerialNumbers, supplier.name());
			PaymentMethod paymentMethod = supplier.paymentMethod();

			VatInformationTexts vatVatInformationTexts = createVatInformationText(clientInvoice.client(), supplier);

			log.info("Calculating commission with net amount: {}, commission rate: {}, supplier country code: {}",
					clientInvoice.netPrice(), clientInvoice.commissionRate(), supplier.countryCode());
			Commission commission = calculateCommission(clientInvoice.netPrice(), clientInvoice.commissionRate(), supplier.countryCode());

			InvoiceAmounts invoiceAmounts = calculateSupplierInvoiceAmounts(clientInvoice);

			BigDecimal amountDue = calculateAmountDue(clientInvoice, commission);

			return new SupplierInvoice(mapClientInfo(clientInvoice),
					mapSupplierInfo(supplier),
					clientInvoice.invoiceDate(),
					clientInvoice.dueDate(),
					invoiceAmounts,
					userMap.get(clientInvoice.id()),
					newSerialNumber,
					paymentMethod,
					amountDue,
					commission,
					vatVatInformationTexts);
		}).toList();
	}

	private static String createSerialNumber(Map<SupplierNameKey, SerialNumber> currentSerialNumbers, String supplierName) {
		SupplierNameKey supplierNameKey = new SupplierNameKey(supplierName);
		int currentSupplierInvoiceCounter = supplierInvoiceCounter.getOrDefault(supplierNameKey, 0);
		SerialNumber newSerialNumber = currentSerialNumbers.get(supplierNameKey).incrementSuffix(1 + currentSupplierInvoiceCounter);
		supplierInvoiceCounter.put(supplierNameKey, currentSupplierInvoiceCounter + 1);
		return newSerialNumber.prefix() + "-" + newSerialNumber.suffix();
	}

	private static BigDecimal calculateAmountDue(ClientInvoice clientInvoice, Commission commission) {
		return clientInvoice.grossPrice().subtract(commission.grossCommission().add(commission.commissionRoundingAmount().orElse(BigDecimal.ZERO).negate()));
	}

	private static SupplierInvoice.ClientInfo mapClientInfo(ClientInvoice clientInvoice) {
		return new SupplierInvoice.ClientInfo(
				clientInvoice.client().name(),
				clientInvoice.invoiceAddress(),
				clientInvoice.client().countryCode(),
				clientInvoice.client().orgNo(),
				clientInvoice.client().vatNumber(),
				clientInvoice.invoiceNr());
	}

	private static SupplierInvoice.SupplierInfo mapSupplierInfo(Supplier supplier) {
		return new SupplierInvoice.SupplierInfo(supplier.id(),
				supplier.name(),
				supplier.address(),
				supplier.supplierReference(),
				supplier.vatNr(),
				supplier.countryCode());
	}
}
