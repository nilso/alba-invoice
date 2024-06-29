package app;

import static app.Table.refreshTable;

import java.util.Map;

import app.domain.ClientInvoiceTableItem;
import config.Config;
import domain.InvoiceId;
import domain.UIData;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import service.UIDataService;

@Slf4j
public class Header {
	private final UIDataService uiDataService;

	public Header(UIDataService uiDataService) {
		this.uiDataService = uiDataService;
	}

	public VBox getHeader(TableView<ClientInvoiceTableItem> table) {
		Label inputFieldLabel = new Label("Antal dagar tillbaka(1-30):");
		TextField refreshDataInputField = createRefreshDataInputField();
		Button refreshDataButton = createRefreshDataButton();
		ProgressIndicator progressIndicator = new ProgressIndicator();

		progressIndicator.setVisible(false);
		refreshDataInputField.setOnAction(event -> refreshAction(refreshDataInputField, table, refreshDataButton, progressIndicator));
		refreshDataButton.setOnAction(event -> refreshAction(refreshDataInputField, table, refreshDataButton, progressIndicator));
		HBox inputField = new HBox(refreshDataInputField, refreshDataButton, progressIndicator);
		return new VBox(inputFieldLabel, inputField);
	}

	private TextField createRefreshDataInputField() {
		return new TextField(String.valueOf(Config.getDefaultDaysBack()));
	}

	private Button createRefreshDataButton() {
		return new Button("Refresh");
	}

	private void refreshAction(TextField textField,
			TableView<ClientInvoiceTableItem> table,
			Button refreshButton,
			ProgressIndicator progressIndicator) {
		String input = textField.getText();
		try {
			int inputValue = Integer.parseInt(input);

			if (inputValue > 30) {
				alert("För många dagar", "Max 30 dagar tillbaka är tillåtet, säg till om mer behövs.");
				log.info("Invalid input {} . Maximum of 30 days allowed", inputValue);
			} else {
				log.info("Refreshing with {} days", inputValue);

				disable(refreshButton, textField, progressIndicator);

				// Fetch the data in a new thread to avoid blocking the UI
				new Thread(() -> {
					try {
						Map<InvoiceId, UIData> uiData = uiDataService.fetchUIData(inputValue);
						Platform.runLater(() -> {
							refreshTable(table, uiData);
							enable(refreshButton, textField, progressIndicator, table);
						});
					} catch (Exception e) {
						Platform.runLater(() -> {
							alert("Något gick fel, kopiera felmeddelandet och spara alba.log", e.getMessage());
							enable(refreshButton, textField, progressIndicator, table);
						});
					}
				}).start();
			}

		} catch (NumberFormatException e) {
			alert("Felaktig inmatning", "Behöver vara en siffra mellan 1 och 30");
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

	private void alert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
