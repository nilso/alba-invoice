package app;

import app.domain.ClientInvoiceTableItem;
import config.Config;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import service.SupplierIdDocumentService;

@Slf4j
public class Header {
	private final TableDataService tableDataService;
	private final Table table;
	private final SupplierIdDocumentService supplierIdDocumentService;

	public Header(TableDataService tableDataService,
			Table table,
			SupplierIdDocumentService supplierIdDocumentService) {
		this.tableDataService = tableDataService;
		this.table = table;
		this.supplierIdDocumentService = supplierIdDocumentService;
	}

	public VBox getHeader(TableView<ClientInvoiceTableItem> table) {
		Label inputFieldLabel = new Label("Antal dagar tillbaka(1-30):");
		TextField refreshDataInputField = createRefreshDataInputField();
		Button refreshDataButton = createRefreshDataButton();
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setVisible(false);
		refreshDataInputField.setOnAction(event -> refreshAction(refreshDataInputField, table, refreshDataButton, progressIndicator));
		refreshDataButton.setOnAction(event -> refreshAction(refreshDataInputField, table, refreshDataButton, progressIndicator));


		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		Button createSupplierIdDocumentButton = createSupplierIdDocumentButton();
		createSupplierIdDocumentButton.setOnAction(event -> {
			try {
				String filePath = supplierIdDocumentService.createSupplierIdDocument();
				alert("Filen är skapad", filePath, AlertType.INFORMATION);
			} catch (Exception e) {
				log.warn("Failed to create supplier id document", e);
				alert("Något gick fel, kopiera felmeddelandet och spara alba.log", e.getMessage(), AlertType.ERROR);
			}
		});

		HBox inputField = new HBox(refreshDataInputField,
				refreshDataButton,
				progressIndicator,
				spacer,
				createSupplierIdDocumentButton);
		return new VBox(inputFieldLabel, inputField);
	}

	private TextField createRefreshDataInputField() {
		return new TextField(String.valueOf(Config.getDefaultDaysBack()));
	}

	private Button createRefreshDataButton() {
		return new Button("Refresh");
	}

	private Button createSupplierIdDocumentButton() {
		return new Button("Skapa fil med klientIdn");
	}

	private void refreshAction(TextField textField,
			TableView<ClientInvoiceTableItem> tableView,
			Button refreshButton,
			ProgressIndicator progressIndicator) {
		String input = textField.getText();
		try {
			int inputValue = Integer.parseInt(input);

			if (inputValue > 30) {
				alert("För många dagar", "Max 30 dagar tillbaka är tillåtet, säg till om mer behövs.", AlertType.ERROR);
				log.info("Invalid input {} . Maximum of 30 days allowed", inputValue);
			} else {
				log.info("Refreshing with {} days", inputValue);

				disable(refreshButton, textField, progressIndicator);

				// Fetch the data in a new thread to avoid blocking the UI
				new Thread(() -> {
					try {
						Platform.runLater(() -> {
							try {
								tableDataService.changeDaysBack(inputValue);
							} catch (Exception e) {
								log.warn("Failed to refresh data", e);
								alert("Något gick fel, kopiera felmeddelandet och spara alba.log", e.getMessage(), AlertType.ERROR);
							}
							table.populateTable();
							enable(refreshButton, textField, progressIndicator, tableView);
						});
					} catch (Exception e) {
						Platform.runLater(() -> {
							alert("Något gick fel, kopiera felmeddelandet och spara alba.log", e.getMessage(), AlertType.ERROR);
							enable(refreshButton, textField, progressIndicator, tableView);
						});
					}
				}).start();
			}

		} catch (NumberFormatException e) {
			alert("Felaktig inmatning", "Behöver vara en siffra mellan 1 och 30", AlertType.ERROR);
			log.info("Invalid input {} . Please enter an integer.", input);
		}
	}

	private void disable(Button refreshButton, TextField textField, ProgressIndicator progressIndicator) {
		progressIndicator.setVisible(true);
		refreshButton.setDisable(true);
		textField.setOnAction(null);
	}

	private void enable(Button refreshButton, TextField textField, ProgressIndicator progressIndicator, TableView<ClientInvoiceTableItem> table) {
		progressIndicator.setVisible(false);
		refreshButton.setDisable(false);
		textField.setOnAction(event -> refreshAction(textField, table, refreshButton, progressIndicator));
	}

	private void alert(String title, String message, AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		Label label = new Label(message);
		label.setWrapText(false);
		label.setMaxWidth(1000);
		alert.getDialogPane().setContent(label);
		alert.showAndWait();
	}
}
