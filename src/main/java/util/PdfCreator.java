package util;

import static util.BigDecimalUtil.bigDecimalToPercent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import domain.Address;
import domain.ClientInvoice;
import domain.InvoiceAmounts;
import domain.PaymentMethod;
import domain.SupplierInvoice;
import domain.SupplierInvoiceRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdfCreator {
	private static final String FILE_SUFFIX = ".pdf";
	static boolean SETemplate = true;
	String SE_TEMPLATE = "selfinvoice_template_SE.pdf";
	String EN_TEMPLATE = "selfinvoice_template_EN.pdf";

	public PdfCreator() {

	}

	public SupplierInvoiceRequest.File createPdf(SupplierInvoice supplierInvoice, LocalDate now) {
		String file;
		SETemplate = supplierInvoice.supplierCountryCode().equals("SE");

		if (SETemplate) {
			file = Objects.requireNonNull(this.getClass().getClassLoader().getResource(SE_TEMPLATE)).getFile();
		} else {
			file = Objects.requireNonNull(this.getClass().getClassLoader().getResource(EN_TEMPLATE)).getFile();
		}

		try (PDDocument document = PDDocument.load(new File(file))) {
			PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
			if (acroForm != null) {
				writesupplierSection(acroForm, supplierInvoice);
				writeInvoiceSection(acroForm, supplierInvoice);
				writeSummarySection(acroForm, supplierInvoice);
				writeRows(acroForm, supplierInvoice);
			} else {
				log.info("No form fields found.");
			}

			String dateString = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/";
			String homeDir = System.getProperty("user.home");
			String fileName;

			if (SETemplate) {
				fileName = "Självfaktura " + supplierInvoice.serialNumber() + ", " + supplierInvoice.supplierReference() + FILE_SUFFIX;
			} else {
				fileName = "Self-billing invoice " + supplierInvoice.serialNumber() + ", " + supplierInvoice.supplierReference() + FILE_SUFFIX;
			}

			String directory = homeDir + "/Documents/" + dateString;
			String filePath = directory + fileName;
			Path path = Paths.get(directory);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			document.save(filePath);
			return new SupplierInvoiceRequest.File(fileName, convertPdfToIntArray(document));
		} catch (IOException e) {
			log.error("An error occurred while trying to read the PDF: ", e);
			throw new RuntimeException(e);
		}
	}

	private static void writesupplierSection(PDAcroForm acroForm, SupplierInvoice supplierInvoice) throws IOException {
		Address address = supplierInvoice.supplierAddress();
		PDField supplierNameField = acroForm.getField("f_supplierName");
		supplierNameField.setValue(supplierInvoice.supplierName());

		PDField supplierAddress1Field = acroForm.getField("f_supplierAddress1");
		supplierAddress1Field.setValue(address.address1());

		PDField supplierPostalField = acroForm.getField("f_supplierPostal");
		supplierPostalField.setValue(address.zipCode() + " " + address.state());

		PDField supplierReference = acroForm.getField("f_supplierReference");
		supplierReference.setValue(supplierInvoice.supplierReference());

		PDField agentName = acroForm.getField("f_agentName");
		agentName.setValue(supplierInvoice.agent().name());

		PDField supplierVatNr = acroForm.getField("f_supplierVatNr");
		supplierVatNr.setValue(supplierInvoice.supplierVatNr());

	}

	private static void writeInvoiceSection(PDAcroForm acroForm, SupplierInvoice supplierInvoice) throws IOException {
		PDField invoiceDateField = acroForm.getField("f_invoiceDate");
		invoiceDateField.setValue(supplierInvoice.invoiceDate());

		PDField dueDateField = acroForm.getField("f_dueDate");
		dueDateField.setValue(supplierInvoice.dueDate());

		PDField serialNumberField = acroForm.getField("f_serialNumber");
		serialNumberField.setValue(supplierInvoice.serialNumber());

		PaymentMethod paymentMethod = supplierInvoice.paymentMethod();

		PDField paymentMethodField = acroForm.getField("f_paymentMethod");
		paymentMethodField.setValue(paymentMethod.name());

		PDField paymentNumberField = acroForm.getField("f_paymentNumber");
		paymentNumberField.setValue(paymentMethod.number());

		if (supplierInvoice.paymentMethod().additionalPaymentMethod().isPresent() && supplierInvoice.paymentMethod().additionalNumber().isPresent()) {
			PDField paymentMethodAdditional = acroForm.getField("f_paymentMethodAdditional");
			paymentMethodAdditional.setValue(paymentMethod.additionalPaymentMethod().get());

			PDField paymentNumberAdditionalField = acroForm.getField("f_paymentNumberAdditional");
			paymentNumberAdditionalField.setValue(paymentMethod.additionalNumber().get());

		}
	}

	private static void writeSummarySection(PDAcroForm acroForm, SupplierInvoice supplierInvoice) throws IOException {
		InvoiceAmounts invoiceAmounts = supplierInvoice.invoiceAmounts();
		String vatRateInPercent = bigDecimalToPercent(invoiceAmounts.vatRate());
		String commissionVatRateInPercent = bigDecimalToPercent(supplierInvoice.commission().commissionVatRate());
		String commissionRateInPercent = bigDecimalToPercent(supplierInvoice.commission().commissionRate());

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
		netPriceField.setValue(invoiceAmounts.netPrice().toString());

		PDField vatField = acroForm.getField("f_vat");
		vatField.setValue(supplierInvoice.invoiceAmounts().vatAmount().toString());

		PDField grossPriceField = acroForm.getField("f_grossPrice");
		grossPriceField.setValue(invoiceAmounts.grossPriceRounded().toString());

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
		netCommissionField.setValue(supplierInvoice.commission().netCommission().negate().toString());

		PDField commissionVatField = acroForm.getField("f_commissionVat");
		commissionVatField.setValue(supplierInvoice.commission().commissionVatAmount().negate().toString());

		PDField grossCommissionField = acroForm.getField("f_grossCommission");
		grossCommissionField.setValue(supplierInvoice.commission().grossCommission().negate().toString());

		if (supplierInvoice.commission().commissionRoundingAmount().isPresent()) {
			PDField commissionRoundingTextField = acroForm.getField("f_commissionRoundingText");
			if (SETemplate) {
				commissionRoundingTextField.setValue("Öresavrundning");
			} else {
				commissionRoundingTextField.setValue("Rounding");
			}

			PDField commissionRoundingAmountField = acroForm.getField("f_commissionRoundingAmount");
			commissionRoundingAmountField.setValue(supplierInvoice.commission().commissionRoundingAmount().get().toString());
		}

		if (supplierInvoice.vatInformationTexts().supplierVatInformationText().isPresent()) {
			PDField reversedVatInformationTextField = acroForm.getField("f_reversedVatInformationText");
			reversedVatInformationTextField.setValue(supplierInvoice.vatInformationTexts().supplierVatInformationText().get());
		}

		PDField amountDueCurrencyField = acroForm.getField("f_amountDueCurrency");
		amountDueCurrencyField.setValue(invoiceAmounts.currency());

		PDField amountDueField = acroForm.getField("f_amountDue");
		amountDueField.setValue(supplierInvoice.amountDue().toString());
	}

	private static void writeRows(PDAcroForm acroForm, SupplierInvoice supplierInvoice) throws IOException {
		ClientInvoice clientInvoice = supplierInvoice.clientInvoice();
		InvoiceAmounts invoiceAmounts = supplierInvoice.invoiceAmounts();
		AtomicInteger rowNo = new AtomicInteger();
		invoiceAmounts.productRows().forEach(productRow -> {
			try {
				if (rowNo.get() > 3) {
					throw new RuntimeException("Too many rows, only supports 4 rows.");
				}

				BigDecimal vat = productRow.netPrice().multiply(productRow.vatRate()).setScale(2, RoundingMode.HALF_UP);
				BigDecimal grossPrice = productRow.netPrice().add(vat).setScale(2, RoundingMode.HALF_UP);

				PDField productDescriptionField = acroForm.getField("f_productRowDescription" + rowNo);
				productDescriptionField.setValue(productRow.description());

				PDField productRowNetPriceField = acroForm.getField("f_productRowNetPrice" + rowNo);
				productRowNetPriceField.setValue(productRow.netPrice().toString());

				PDField productRowVatField = acroForm.getField("f_productRowVat" + rowNo);
				productRowVatField.setValue(vat.toString());

				PDField productRowGrossPriceField = acroForm.getField("f_productRowGrossPrice" + rowNo);
				productRowGrossPriceField.setValue(grossPrice.toString());
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
				roundingAmountField.setValue(invoiceAmounts.roundingAmount().get().negate().toString());
			}

			PDField clientNameField = acroForm.getField("f_clientName");
			clientNameField.setValue(clientInvoice.client().name());

			Address address = clientInvoice.invoiceAddress();
			PDField clientAddress1Field = acroForm.getField("f_clientAddress1");
			clientAddress1Field.setValue(address.address1());

			PDField clientAddress2Field = acroForm.getField("f_clientAddress2");
			clientAddress2Field.setValue(address.address2());

			PDField clientPostalField = acroForm.getField("f_clientPostal");
			clientPostalField.setValue(address.zipCode() + " " + address.state());

			PDField clientIdentificationNumberField = acroForm.getField("f_clientIdentificationNumber");
			if (clientInvoice.client().countryCode().equals("SE")) {
				if (SETemplate) {
					clientIdentificationNumberField.setValue("Org.nr: " + clientInvoice.client().orgNo());
				} else {
					clientIdentificationNumberField.setValue("Registration.no: " + clientInvoice.client().orgNo());
				}
			} else {
				if (SETemplate) {
					clientIdentificationNumberField.setValue("Vat.nr: " + clientInvoice.client().vatNumber());
				} else {
					clientIdentificationNumberField.setValue("Vat.no: " + clientInvoice.client().vatNumber());
				}

			}

			PDField clientInvoiceNumberField = acroForm.getField("f_clientInvoiceNumber");
			if (SETemplate) {
				clientInvoiceNumberField.setValue("Fakturareferens Albatros: " + clientInvoice.invoiceNr());
			} else {
				clientInvoiceNumberField.setValue("Invoice reference Albatros: " + clientInvoice.invoiceNr());
			}

			if (supplierInvoice.vatInformationTexts().clientVatInformationText().isPresent()) {
				PDField vatInformationTextField = acroForm.getField("f_vatInformationText");
				vatInformationTextField.setValue(supplierInvoice.vatInformationTexts().clientVatInformationText().get());
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
}