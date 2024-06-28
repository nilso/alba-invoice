package service;

import static util.CommissionCalculator.calculateCommission;
import static util.VatInformationTextUtil.createVatInformationText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.ClientInvoice;
import domain.Commission;
import domain.InvoiceId;
import domain.PaymentMethod;
import domain.ProductRow;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierInvoice;
import domain.SupplierNameKey;
import domain.User;
import domain.VatInformationTexts;
import lombok.extern.slf4j.Slf4j;
import util.BigDecimalUtil;

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

			BigDecimal vatRate = calculateVatRate(clientInvoice);
			BigDecimal vatAmount = calculateVatAmount(clientInvoice);
			VatInformationTexts vatVatInformationTexts = createVatInformationText(clientInvoice.client(), supplier);

			log.info("Calculating commission with net amount: {}, commission rate: {}, supplier country code: {}",
					clientInvoice.netPrice(), clientInvoice.commissionRate(), supplier.countryCode());
			Commission commission = calculateCommission(clientInvoice.netPrice(), clientInvoice.commissionRate(), supplier.countryCode());

			BigDecimal amountDue = calculateAmountDue(clientInvoice, commission);
			return new SupplierInvoice(clientInvoice.supplierId(),
					supplier.name(),
					supplier.address(),
					clientInvoice,
					supplier.supplierReference(),
					userMap.get(clientInvoice.id()),
					supplier.vatNr(),
					supplier.countryCode(),
					newSerialNumber,
					paymentMethod,
					vatRate,
					vatAmount,
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

	private static BigDecimal calculateVatRate(ClientInvoice clientInvoice) {
		return clientInvoice.productRows().getFirst().vatRate();
	}

	private static BigDecimal calculateVatAmount(ClientInvoice clientInvoice) {
		BigDecimal vatAmount = BigDecimal.ZERO;
		for (ProductRow productRow : clientInvoice.productRows()) {
			BigDecimal vatRate = productRow.vatRate();
			BigDecimal netPrice = productRow.netPrice();
			BigDecimal vat = netPrice.multiply(vatRate);
			vatAmount = vatAmount.add(vat);
		}
		return vatAmount.setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal calculateAmountDue(ClientInvoice clientInvoice, Commission commission) {
		return clientInvoice.grossPrice().subtract(commission.grossCommission().add(commission.commissionRoundingAmount().orElse(BigDecimal.ZERO).negate()));
	}
}
