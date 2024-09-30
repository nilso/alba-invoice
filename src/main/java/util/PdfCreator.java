package util;

import static util.BigDecimalUtil.bigDecimalToPercent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import domain.Address;
import domain.InvoiceAmounts;
import domain.PaymentMethod;
import domain.SupplierInvoiceData;
import domain.SupplierInvoiceRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdfCreator {
	private static final String FILE_SUFFIX = ".pdf";
	private static boolean SETemplate = true;
	String SE_TEMPLATE = "/selfinvoice_template_SE.pdf";
	String EN_TEMPLATE = "/selfinvoice_template_EN.pdf";

	public PdfCreator() {

	}

	public SupplierInvoiceRequest.File createPdf(SupplierInvoiceData supplierInvoiceData) {
		InputStream pdfStream;
		SETemplate = supplierInvoiceData.supplierInfo().countryCode().equals("SE");

		if (SETemplate) {
			pdfStream = Objects.requireNonNull(getClass().getResourceAsStream(SE_TEMPLATE));
		} else {
			pdfStream = Objects.requireNonNull(getClass().getResourceAsStream(EN_TEMPLATE));
		}

		try (PDDocument document = PDDocument.load(pdfStream)) {
			PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
			if (acroForm != null) {
				writeSupplierSection(acroForm, supplierInvoiceData);
				writeInvoiceSection(acroForm, supplierInvoiceData);
				writeSummarySection(acroForm, supplierInvoiceData);
				writeRows(acroForm, supplierInvoiceData);
			} else {
				log.info("No form fields found.");
			}

			String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/";
			String homeDir = System.getProperty("user.home");
			String fileName;

			if (SETemplate) {
				fileName = "Självfaktura " + supplierInvoiceData.serialNumber() + ", " + supplierInvoiceData.supplierInfo().reference() + FILE_SUFFIX;
			} else {
				fileName = "Self-billing invoice " + supplierInvoiceData.serialNumber() + ", " + supplierInvoiceData.supplierInfo().reference() + FILE_SUFFIX;
			}

			String directory = homeDir + "/Documents/" + dateString;
			String filePath = directory + fileName;
			Path path = Paths.get(directory);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			document.save(filePath);
			return new SupplierInvoiceRequest.File(filePath, convertPdfToIntArray(document));
		} catch (IOException e) {
			log.error("An error occurred while trying to read the PDF: ", e);
			throw new RuntimeException(e);
		}
	}

	private static void writeSupplierSection(PDAcroForm acroForm, SupplierInvoiceData supplierInvoiceData) throws IOException {
		SupplierInvoiceData.SupplierInfo supplierInfo = supplierInvoiceData.supplierInfo();
		Address address = supplierInfo.address();
		PDField supplierNameField = acroForm.getField("f_supplierName");
		supplierNameField.setValue(supplierInfo.name());

		PDField supplierAddress1Field = acroForm.getField("f_supplierAddress1");
		supplierAddress1Field.setValue(address.address1());

		PDField supplierPostalField = acroForm.getField("f_supplierPostal");
		supplierPostalField.setValue(address.zipCode() + " " + address.state());

		PDField supplierReference = acroForm.getField("f_supplierReference");
		supplierReference.setValue(supplierInfo.reference());

		PDField agentName = acroForm.getField("f_agentName");
		agentName.setValue(supplierInvoiceData.agent().name());

		PDField supplierVatNr = acroForm.getField("f_supplierVatNr");
		supplierVatNr.setValue(supplierInfo.vatNr());

	}

	private static void writeInvoiceSection(PDAcroForm acroForm, SupplierInvoiceData supplierInvoiceData) throws IOException {
		PDField invoiceDateField = acroForm.getField("f_invoiceDate");
		invoiceDateField.setValue(supplierInvoiceData.invoiceDate());

		PDField dueDateField = acroForm.getField("f_dueDate");
		dueDateField.setValue(supplierInvoiceData.dueDate());

		PDField serialNumberField = acroForm.getField("f_serialNumber");
		serialNumberField.setValue(supplierInvoiceData.serialNumber());

		PaymentMethod paymentMethod = supplierInvoiceData.paymentMethod();

		PDField paymentMethodField = acroForm.getField("f_paymentMethod");
		paymentMethodField.setValue(paymentMethod.name());

		PDField paymentNumberField = acroForm.getField("f_paymentNumber");
		paymentNumberField.setValue(paymentMethod.number());

		if (supplierInvoiceData.paymentMethod().additionalPaymentMethod().isPresent() && supplierInvoiceData.paymentMethod().additionalNumber().isPresent()) {
			PDField paymentMethodAdditional = acroForm.getField("f_paymentMethodAdditional");
			paymentMethodAdditional.setValue(paymentMethod.additionalPaymentMethod().get());

			PDField paymentNumberAdditionalField = acroForm.getField("f_paymentNumberAdditional");
			paymentNumberAdditionalField.setValue(paymentMethod.additionalNumber().get());

		}
	}

	private static void writeSummarySection(PDAcroForm acroForm, SupplierInvoiceData supplierInvoiceData) throws IOException {
		InvoiceAmounts invoiceAmounts = supplierInvoiceData.invoiceAmounts();
		String vatRateInPercent = bigDecimalToPercent(invoiceAmounts.vatRate());
		String commissionVatRateInPercent = bigDecimalToPercent(supplierInvoiceData.commission().commissionVatRate());
		String commissionRateInPercent = bigDecimalToPercent(supplierInvoiceData.commission().commissionRate());

		PDField priceCurrencyField = acroForm.getField("f_priceCurrency");
		priceCurrencyField.setValue("A-pris " + invoiceAmounts.currency());
		if (SETemplate) {
			priceCurrencyField.setValue("A-pris " + invoiceAmounts.currency());
		} else {
			priceCurrencyField.setValue("Price " + invoiceAmounts.currency());
		}

		PDField vatRateField = acroForm.getField("f_vatRate");
		if (SETemplate) {
			vatRateField.setValue("Moms " + vatRateInPercent + "%");
		} else {
			vatRateField.setValue("VAT " + vatRateInPercent + "%");
		}

		PDField netPriceField = acroForm.getField("f_netPrice");
		netPriceField.setValue(formatBigDecimal(invoiceAmounts.netPrice()));

		PDField vatField = acroForm.getField("f_vat");
		vatField.setValue(formatBigDecimal(supplierInvoiceData.invoiceAmounts().vatAmount()));

		PDField grossPriceField = acroForm.getField("f_grossPrice");
		grossPriceField.setValue(formatBigDecimal(invoiceAmounts.grossPriceRounded()));

		PDField commissionCurrencyField = acroForm.getField("f_commissionCurrency");
		if (SETemplate) {
			commissionCurrencyField.setValue("A-pris " + invoiceAmounts.currency());
		} else {
			commissionCurrencyField.setValue("Price " + invoiceAmounts.currency());
		}

		PDField commissionVatRateField = acroForm.getField("f_commissionVatRate");
		if (SETemplate) {
			commissionVatRateField.setValue("Moms " + commissionVatRateInPercent + "%");
		} else {
			commissionVatRateField.setValue("VAT " + commissionVatRateInPercent + "%");
		}

		PDField commissionRateField = acroForm.getField("f_commissionRate");
		commissionRateField.setValue(commissionRateInPercent + "%");

		PDField netCommissionField = acroForm.getField("f_netCommission");
		netCommissionField.setValue(formatBigDecimal(supplierInvoiceData.commission().netCommission().negate()));

		PDField commissionVatField = acroForm.getField("f_commissionVat");
		commissionVatField.setValue(formatBigDecimal(supplierInvoiceData.commission().commissionVatAmount().negate()));

		PDField grossCommissionField = acroForm.getField("f_grossCommission");
		grossCommissionField.setValue(formatBigDecimal(supplierInvoiceData.commission().grossCommission().negate()));

		if (supplierInvoiceData.commission().commissionRoundingAmount().isPresent()) {
			PDField commissionRoundingTextField = acroForm.getField("f_commissionRoundingText");
			if (SETemplate) {
				commissionRoundingTextField.setValue("Öresavrundning");
			} else {
				commissionRoundingTextField.setValue("Rounding");
			}

			PDField commissionRoundingAmountField = acroForm.getField("f_commissionRoundingAmount");
			commissionRoundingAmountField.setValue(formatBigDecimal(supplierInvoiceData.commission().commissionRoundingAmount().get()));
		}

		if (supplierInvoiceData.vatInformationTexts().supplierVatInformationText().isPresent()) {
			PDField reversedVatInformationTextField = acroForm.getField("f_reversedVatInformationText");
			reversedVatInformationTextField.setValue(supplierInvoiceData.vatInformationTexts().supplierVatInformationText().get());
		}

		PDField amountDueCurrencyField = acroForm.getField("f_amountDueCurrency");
		amountDueCurrencyField.setValue(invoiceAmounts.currency());

		PDField amountDueField = acroForm.getField("f_amountDue");
		amountDueField.setValue(formatBigDecimal(supplierInvoiceData.amountDue()));
	}

	private static void writeRows(PDAcroForm acroForm, SupplierInvoiceData supplierInvoiceData) throws IOException {
		SupplierInvoiceData.ClientInfo clientInfo = supplierInvoiceData.clientInfo();
		InvoiceAmounts invoiceAmounts = supplierInvoiceData.invoiceAmounts();
		AtomicInteger rowNo = new AtomicInteger();
		invoiceAmounts.productRows().forEach(productRow -> {
			try {
				if (rowNo.get() > 3) {
					throw new RuntimeException("Too many rows, only supports 4 rows.");
				}

				BigDecimal vat = productRow.netPrice().multiply(productRow.vatRate()).setScale(2, RoundingMode.HALF_UP);
				BigDecimal grossPrice = productRow.netPrice().add(vat).setScale(2, RoundingMode.HALF_UP);

				PDField productDescriptionField = acroForm.getField("f_productRowDescription" + rowNo);
				productDescriptionField.setValue(productRow.description().replace("\n", " "));

				PDField productRowNetPriceField = acroForm.getField("f_productRowNetPrice" + rowNo);
				productRowNetPriceField.setValue(formatBigDecimal(productRow.netPrice()));

				PDField productRowVatField = acroForm.getField("f_productRowVat" + rowNo);
				productRowVatField.setValue(formatBigDecimal(vat));

				PDField productRowGrossPriceField = acroForm.getField("f_productRowGrossPrice" + rowNo);
				productRowGrossPriceField.setValue(formatBigDecimal(grossPrice));
				rowNo.getAndIncrement();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		});

		try {

			if (invoiceAmounts.roundingAmount().isPresent()) {
				PDField roundingTextField = acroForm.getField("f_productRowRoundingText");
				if (SETemplate) {
					roundingTextField.setValue("Öresavrundning");
				} else {
					roundingTextField.setValue("Rounding");
				}

				PDField roundingAmountField = acroForm.getField("f_productRowRoundingAmount");
				roundingAmountField.setValue(formatBigDecimal(invoiceAmounts.roundingAmount().get().negate()));
			}

			PDField clientNameField = acroForm.getField("f_clientName");
			clientNameField.setValue(clientInfo.name());

			Address address = clientInfo.invoiceAddress();
			PDField clientAddress1Field = acroForm.getField("f_clientAddress1");
			clientAddress1Field.setValue(address.address1());

			PDField clientAddress2Field = acroForm.getField("f_clientAddress2");
			clientAddress2Field.setValue(address.address2());

			PDField clientPostalField = acroForm.getField("f_clientPostal");
			clientPostalField.setValue(address.zipCode() + " " + address.state() + ", " + address.country());

			PDField clientIdentificationNumberField = acroForm.getField("f_clientIdentificationNumber");
			if (clientInfo.countryCode().equals("SE")) {
				if (SETemplate) {
					clientIdentificationNumberField.setValue("Org.nr: " + clientInfo.orgNo());
				} else {
					clientIdentificationNumberField.setValue("Registration.no: " + clientInfo.orgNo());
				}
			} else {
				if (SETemplate) {
					clientIdentificationNumberField.setValue("Vat.nr: " + clientInfo.vatNumber());
				} else {
					clientIdentificationNumberField.setValue("Vat.no: " + clientInfo.vatNumber());
				}

			}

			PDField clientInvoiceNumberField = acroForm.getField("f_clientInvoiceNumber");
			if (SETemplate) {
				clientInvoiceNumberField.setValue("Fakturareferens Albatros: " + clientInfo.invoiceNr());
			} else {
				clientInvoiceNumberField.setValue("Invoice reference Albatros: " + clientInfo.invoiceNr());
			}

			if (supplierInvoiceData.vatInformationTexts().clientVatInformationText().isPresent()) {
				PDField vatInformationTextField = acroForm.getField("f_vatInformationText");
				vatInformationTextField.setValue(supplierInvoiceData.vatInformationTexts().clientVatInformationText().get());
			}

		} catch (IOException e) {
			log.error("An error occurred while trying to write the PDF: ", e);
			throw new RuntimeException(e);
		}

	}

	public static int[] convertPdfToIntArray(PDDocument document) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			document.save(byteArrayOutputStream);
			return convertByteArrayToIntArray(byteArrayOutputStream.toByteArray());
		}
	}

	public static int[] convertByteArrayToIntArray(byte[] byteArray) {
		int[] intArray = new int[byteArray.length];
		for (int i = 0; i < byteArray.length; i++) {
			intArray[i] = byteArray[i] & 0xFF;  // Convert each byte to an unsigned integer
		}
		return intArray;
	}

	public static String formatBigDecimal(BigDecimal number) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		symbols.setGroupingSeparator(' ');
		symbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
		decimalFormat.setGroupingSize(3);
		decimalFormat.setParseBigDecimal(true);
		return decimalFormat.format(number);
	}
}