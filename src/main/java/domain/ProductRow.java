package domain;

import java.math.BigDecimal;

public record ProductRow(
		BigDecimal netPrice,
		BigDecimal vatRate,
		Product product,
		String description
) {
}
