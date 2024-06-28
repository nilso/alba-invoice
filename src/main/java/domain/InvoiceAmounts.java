package domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public record InvoiceAmounts(
		String currency,
		BigDecimal netPrice,
		BigDecimal grossPriceRounded,
		BigDecimal vatAmount,
		BigDecimal vatRate,
		Optional<BigDecimal> roundingAmount,
		List<ProductRow> productRows
) {
}
