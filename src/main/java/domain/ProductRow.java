package domain;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record ProductRow(
		BigDecimal netPrice,
		BigDecimal vatRate,
		Product product,
		String description
) {
}
