package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import domain.BankAccount;
import domain.BankAccountId;
import domain.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierIdDocumentCreator {

	String FILE_NAME = "klientId";
	String FILE_SUFFIX = ".xlsx";

	public String createExcelFile(List<Supplier> suppliers, Map<BankAccountId, BankAccount> bankAccounts) {
		log.info("Creating Excel file for suppliers: {}", suppliers);
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("klientIdn");
			int nrOfColumns = setHeaders(sheet);

			int rowIndex = 1;
			for (Supplier supplier : suppliers) {
				try {
					BankAccount bankAccount;
					if (supplier.bankAccountId().isPresent()) {
						bankAccount = bankAccounts.get(supplier.bankAccountId().get());
					} else {
						bankAccount = new BankAccount(new BankAccountId("0"), "SEK", "No bank account found");
					}

					log.info("Writing supplier: {} with bankAccount {}", supplier, bankAccount);
					writeRow(sheet, rowIndex++, supplier, bankAccount);

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
			String filePath = homeDir + "/Documents/" + FILE_NAME + "_" + LocalDate.now() + FILE_SUFFIX;
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

			return filePath;
		} catch (IOException e) {
			throw new RuntimeException("Failed creating excel", e);
		}
	}

	private static int setHeaders(Sheet sheet) {
		// Create a header row
		Row headerRow = sheet.createRow(0);
		String[] columns = { "Id",
				"Klient namn",
				"Valuta",
				"Betalningsmetod"
		};
		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
		}

		return columns.length;
	}

	private static void writeRow(Sheet sheet,
			int rowNr,
			Supplier supplier,
			BankAccount bankAccount) throws ParseException {

		String paymentMethodNumber;
		if (supplier.paymentMethod().additionalNumber().isPresent()) {
			paymentMethodNumber = supplier.paymentMethod().additionalNumber().get();
		} else {
			paymentMethodNumber = supplier.paymentMethod().number();
		}

		// Populate data
		Row dataRow = sheet.createRow(rowNr);
		dataRow.createCell(0).setCellValue(supplier.id().getId());
		dataRow.createCell(1).setCellValue(supplier.name());
		dataRow.createCell(2).setCellValue(bankAccount.currency());
		dataRow.createCell(3).setCellValue(supplier.paymentMethod().name() + " " + paymentMethodNumber);
	}
}
