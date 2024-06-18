package domain;

import java.util.Optional;

public record VatInformationTexts(Optional<String> clientVatInformationText, Optional<String> supplierVatInformationText) {
}
