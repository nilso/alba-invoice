package util;

import static util.EUCountryCodes.EU_COUNTRY_CODES;

import java.util.Optional;

import domain.Client;
import domain.Supplier;
import domain.VatInformationTexts;

public class VatInformationTextUtil {
	private static final String EU_INFORMATION_EN = "Reverse charge: General rule for services in accordance with Article 44 and 196, Council Directive 2006/112/EC";
	private static final String EU_INFORMATION_SE = "För denna del av fakturan gäller omvänd betalningsskyldighet i enlighet med"
			+ " artikel 44 och artikel 196 i mervärdesskattedirektivet (2006/112/EG)";
	private static final String FOREIGN_INFORMATION_EN = "Outside-Community supply: General rule for services in accordance with Article 44, Council Directive 2006/112/EC";
	private static final String FOREIGN_INFORMATION_SE = "Omsättning utanför EU, svensk moms utgår inte enligt 6 kap. 34 § ML (2023:200)";

	public static VatInformationTexts createVatInformationText(Client client, Supplier supplier) {

		if (client.countryCode().equals("SE")) {
			return handleSEClient(supplier);
		}

		if (EU_COUNTRY_CODES.contains(client.countryCode())) {
			return handleEUClient(supplier);
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

	private static VatInformationTexts handleSEClient(Supplier supplier) {
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

	private static VatInformationTexts handleEUClient(Supplier supplier) {
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
}
