package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import domain.ClientInvoice;
import domain.InvoiceId;
import domain.ProductRow;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierInvoiceExcel {

	String FILE_NAME = "invoices";
	String FILE_SUFFIX = ".xlsx";

	public void createExcelFile(List<ClientInvoice> invoices,
			Map<InvoiceId, User> userMap,
			Map<SupplierId, SerialNumber> newSerialNumbersMap,
			Map<InvoiceId, Supplier> supplierMap) {

		log.info("Creating Excel file for invoice: {}", invoices);
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Invoices");
			int nrOfColumns = setHeaders(sheet);

			int rowIndex = 1;
			for (ClientInvoice invoice : invoices) {
				try {
					for (ProductRow productRow : invoice.productRows()) {
						log.info("Writing Row: {} for invoice: {}", productRow, invoice);
						writeRow(sheet, invoice, productRow, rowIndex++, userMap, newSerialNumbersMap, supplierMap);
					}
				} catch (ParseException e) {
					log.error("Failed writing row: " + e);
				}
			}

			// Resize all columns to fit the content size
			for (int i = 0; i < nrOfColumns; i++) {
				sheet.autoSizeColumn(i);
			}

			// Write the output to a file
			String homeDir = System.getProperty("user.home");
			String filePath = homeDir + "/Documents/" + FILE_NAME + "_" + Instant.now() + FILE_SUFFIX;
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				log.info("Writing Excel file to: {}", filePath);
				workbook.write(fileOut);
			}
		} catch (IOException e) {
			log.error("failed creating excel: " + e);
		}
	}

	private static int setHeaders(Sheet sheet) {
		// Create a header row
		Row headerRow = sheet.createRow(0);
		String[] columns = { "Kundfaktura ID",
				"Fakturanummer",
				"Er referens(säljaren)",
				"Vår referens",
				"Gatuadress",
				"Postnr och ort",
				"Land",
				"Fakturadatum",
				"Förfallodatum",
				"Belopp originalfakturan",
				"Moms",
				"Valuta",
				"Agentarvode procent",
				"Agentarvode belopp",
				"Agentarvode momssats",
				"Betalningsmetod",
				"Betalningsmetod nr",
		};
		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
		}

		return columns.length;
	}

	private static void writeRow(Sheet sheet,
			ClientInvoice invoice,
			ProductRow productRow,
			int rowNr,
			Map<InvoiceId, User> userMap,
			Map<SupplierId, SerialNumber> newSerialNumbers,
			Map<InvoiceId, Supplier> supplierMap) throws ParseException {

		String serialNumber = newSerialNumbers.get(invoice.supplierId()).prefix() + "-" + newSerialNumbers.get(invoice.supplierId()).suffix();
		BigDecimal commissionVatRate = calculateCommissionVatRate(supplierMap.get(invoice.id()));
		BigDecimal commission = calculateCommission(productRow, invoice);

		// Populate data
		Row dataRow = sheet.createRow(rowNr);
		dataRow.createCell(0).setCellValue(invoice.id().getId());
		dataRow.createCell(1).setCellValue(serialNumber);
		dataRow.createCell(2).setCellValue(invoice.yourReference());
		dataRow.createCell(3).setCellValue(userMap.get(invoice.id()).name());
		dataRow.createCell(4).setCellValue(invoice.invoiceAddress().address1());
		dataRow.createCell(5).setCellValue(invoice.invoiceAddress().zipCode() + " " + invoice.invoiceAddress().state());
		dataRow.createCell(6).setCellValue(invoice.invoiceAddress().country());
		dataRow.createCell(7).setCellValue(invoice.invoiceDate());
		dataRow.createCell(8).setCellValue(invoice.dueDate());
		dataRow.createCell(9).setCellValue(invoice.netPrice().toString());
		dataRow.createCell(10).setCellValue(productRow.vatRate().toString());
		dataRow.createCell(11).setCellValue(invoice.currency());
		dataRow.createCell(12).setCellValue(invoice.commissionRate().toString());
		dataRow.createCell(13).setCellValue(commission.toString());
		dataRow.createCell(14).setCellValue(commissionVatRate.toString());
		dataRow.createCell(15).setCellValue(supplierMap.get(invoice.id()).paymentMethod().name());
		dataRow.createCell(16).setCellValue(supplierMap.get(invoice.id()).paymentMethod().number());
	}

	private static BigDecimal calculateCommissionVatRate(Supplier supplier) {
		if (supplier.countryCode().equals("SE")) {
			return new BigDecimal("0.25");
		}

		return new BigDecimal("0.0");
	}

	private static BigDecimal calculateCommission(ProductRow productRow, ClientInvoice invoice) {
		return productRow.netPrice().multiply(invoice.commissionRate()).setScale(2, RoundingMode.HALF_UP);
	}

}
