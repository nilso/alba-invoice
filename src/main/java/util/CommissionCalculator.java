package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import domain.Commission;

public class CommissionCalculator {
	public static Commission calculateCommission(BigDecimal clientInvoiceNetAmount, BigDecimal commissionRate, String supplierCountryCode) {
		BigDecimal commissionVatRate = calculateCommissionVatRate(supplierCountryCode);
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
}
