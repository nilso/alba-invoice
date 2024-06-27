package domain;

import java.math.BigDecimal;
import java.util.Optional;

public record Commission(
		BigDecimal netCommission,
		BigDecimal grossCommission,
		BigDecimal commissionVatRate,
		BigDecimal commissionVatAmount,
		BigDecimal commissionRate,
		Optional<BigDecimal> commissionRoundingAmount
) {
}

