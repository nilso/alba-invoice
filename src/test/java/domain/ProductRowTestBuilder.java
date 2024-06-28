package domain;

import java.math.BigDecimal;

import lombok.Builder;

@Builder()
public class ProductRowTestBuilder {
	@Builder.Default
	BigDecimal netPrice = new BigDecimal(100);
	@Builder.Default
	BigDecimal vatRate = new BigDecimal("0.25");
	@Builder.Default
	Product product = new Product(12456);
	@Builder.Default
	String description = "description";

	public ProductRow aProductRow() {
		return ProductRow.builder()
				.netPrice(netPrice)
				.vatRate(vatRate)
				.product(product)
				.description(description)
				.build();
	}
}
