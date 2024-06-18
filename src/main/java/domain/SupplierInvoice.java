package domain;

import java.math.BigDecimal;
import java.util.Optional;

public record SupplierInvoice(
		SupplierId supplierId,
		String supplierName,
		Address supplierAddress,
		ClientInvoice clientInvoice,
		String supplierReference,
		String agentReference,
		String supplierVatNr,
		String supplierCountryCode,
		String serialNumber,
		PaymentMethod paymentMethod,
		BigDecimal vatRate,
		BigDecimal vatAmount,
		BigDecimal grossPrice,
		Optional<BigDecimal> commissionRoundingAmount,
		BigDecimal netCommissionPrice,
		BigDecimal grossCommissionPrice, //TODO ska inte ta bort öresavrundningen.
		BigDecimal commissionVatRate,
		BigDecimal commissionVatAmount,
		VatInformationTexts vatInformationTexts
) {

}
