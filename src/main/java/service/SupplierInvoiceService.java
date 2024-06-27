package service;

import static util.EUCountryCodes.EU_COUNTRY_CODES;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.Client;
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
			String supplierCountryCode = supplier.countryCode();
			BigDecimal vatRate = calculateVatRate(clientInvoice);
			BigDecimal vatAmount = calculateVatAmount(clientInvoice);
			Commission commission = createCommission(clientInvoice.netPrice(), clientInvoice.commissionRate(), calculateCommissionVatRate(supplierCountryCode));
			VatInformationTexts vatVatInformationTexts = createVatInformationText(clientInvoice.client(), supplier);
			log.info("calculating amountDue with grossPrice: {}, commission: {}, commissionRoundingAmount: {}", clientInvoice.grossPrice(), commission.grossCommission(), commission.commissionRoundingAmount().orElse(BigDecimal.ZERO));
			BigDecimal amountDue = clientInvoice.grossPrice().subtract(commission.grossCommission().add(commission.commissionRoundingAmount().orElse(BigDecimal.ZERO).negate()));
			return new SupplierInvoice(clientInvoice.supplierId(),
					supplier.name(),
					supplier.address(),
					clientInvoice,
					supplier.supplierReference(),
					userMap.get(clientInvoice.id()).name(),
					supplier.vatNr(),
					supplierCountryCode,
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

	public Commission createCommission(BigDecimal clientInvoiceNetAmount, BigDecimal commissionRate, BigDecimal commissionVatRate) {
		log.info("Calculating commission for net amount: {}, commission rate: {}, vat rate: {}", clientInvoiceNetAmount, commissionRate, commissionVatRate);
		BigDecimal netCommission = clientInvoiceNetAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

		BigDecimal commissionVatAmount = netCommission.multiply(commissionVatRate).setScale(2, RoundingMode.HALF_UP);

		BigDecimal grossCommission = netCommission.add(commissionVatAmount).setScale(2, RoundingMode.HALF_UP);

		Optional<BigDecimal> commissionRoundingAmount = BigDecimalUtil.extractDecimalPartIfNotZero(grossCommission);

		return new Commission(netCommission, grossCommission, commissionVatRate, commissionVatAmount, commissionRate, commissionRoundingAmount);
	}

	private static BigDecimal calculateCommissionVatRate(String supplierCountryCode) {
		if (supplierCountryCode.equals("SE")) {
			return BigDecimal.valueOf(0.25);
		} else {
			return BigDecimal.ZERO;
		}
	}

	private static VatInformationTexts createVatInformationText(Client client, Supplier supplier) {

		String EU_INFORMATION_EN = "Reverse charge: General rule for services in accordance with Article 44 and 196, Council Directive 2006/112/EC";
		String EU_INFORMATION_SE = "För denna del av fakturan gäller omvänd betalningsskyldighet i enlighet med"
				+ " artikel 44 och artikel 196 i mervärdesskattedirektivet (2006/112/EG)";
		String FOREIGN_INFORMATION_EN = "Outside-Community supply: General rule for services in accordance with Article 44, Council Directive 2006/112/EC";
		String FOREIGN_INFORMATION_SE = "Omsättning utanför EU, svensk moms utgår inte enligt 6 kap. 34 § ML (2023:200)";

		if (client.countryCode().equals("SE")) {
			if (EU_COUNTRY_CODES.contains(supplier.countryCode())) {
				return new VatInformationTexts(Optional.of(EU_INFORMATION_EN),
						Optional.of(EU_INFORMATION_EN));
			}

			if (!supplier.countryCode().equals("SE")) {
				return new VatInformationTexts(Optional.of(FOREIGN_INFORMATION_EN),
						Optional.of(FOREIGN_INFORMATION_EN));
			}

			return new VatInformationTexts(Optional.empty(),
					Optional.empty());

		}

		if (EU_COUNTRY_CODES.contains(client.countryCode())) {
			if (supplier.countryCode().equals("SE")) {
				return new VatInformationTexts(Optional.of(EU_INFORMATION_SE),
						Optional.empty());
			}

			if (EU_COUNTRY_CODES.contains(supplier.countryCode())) {
				return new VatInformationTexts(Optional.of(EU_INFORMATION_EN),
						Optional.of(EU_INFORMATION_EN));
			}

			return new VatInformationTexts(Optional.of(FOREIGN_INFORMATION_EN),
					Optional.of(FOREIGN_INFORMATION_EN));
		}

		if (supplier.countryCode().equals("SE")) {
			return new VatInformationTexts(Optional.of(FOREIGN_INFORMATION_SE),
					Optional.empty());
		}

		if (EU_COUNTRY_CODES.contains(supplier.countryCode())) {
			return new VatInformationTexts(Optional.of(FOREIGN_INFORMATION_EN),
					Optional.of(EU_INFORMATION_EN));
		}

		return new VatInformationTexts(Optional.of(FOREIGN_INFORMATION_EN),
				Optional.of(FOREIGN_INFORMATION_EN));
	}
}
