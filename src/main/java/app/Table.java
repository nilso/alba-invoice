package app;

import static util.BigDecimalUtil.bigDecimalToPercent;
import static util.BigDecimalUtil.doubleToPercent;
import static util.BigDecimalUtil.parsePercentStringToDecimal;

import java.util.List;

import app.domain.ClientInvoiceTableItem;
import domain.ClientInvoice;
import domain.SerialNumber;
import domain.UIData;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Table {
	public static TableView<ClientInvoiceTableItem> createTable() {
		TableView<ClientInvoiceTableItem> table = new TableView<>();

		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<ClientInvoiceTableItem, String> clientNameColumn = createClientNameColumn();

		TableColumn<ClientInvoiceTableItem, String> invoiceNrColumn = createInvoiceNrColumn();

		TableColumn<ClientInvoiceTableItem, Number> grossPriceColumn = createGrossPriceColumn();

		TableColumn<ClientInvoiceTableItem, String> commissionRateColumn = createCommissionRateColumn(table);

		TableColumn<ClientInvoiceTableItem, String> serialNumberColumn = createSerialNumberColumn(table);

		table.getColumns().add(clientNameColumn);
		table.getColumns().add(invoiceNrColumn);
		table.getColumns().add(grossPriceColumn);
		table.getColumns().add(commissionRateColumn);
		table.getColumns().add(serialNumberColumn);

		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));

		return table;
	}

	private static TableColumn<ClientInvoiceTableItem, String> createClientNameColumn() {
		TableColumn<ClientInvoiceTableItem, String> clientNameColumn = new TableColumn<>("Fakturamottagare");
		clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));
		clientNameColumn.setMinWidth(200);
		return clientNameColumn;
	}

	private static TableColumn<ClientInvoiceTableItem, String> createInvoiceNrColumn() {
		TableColumn<ClientInvoiceTableItem, String> invoiceNrColumn = new TableColumn<>("Fakturanummer");
		invoiceNrColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().invoiceNr()));
		invoiceNrColumn.setMinWidth(100);
		return invoiceNrColumn;
	}

	private static TableColumn<ClientInvoiceTableItem, Number> createGrossPriceColumn() {
		TableColumn<ClientInvoiceTableItem, Number> grossPriceColumn = new TableColumn<>("Belopp");
		grossPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().grossPrice()));
		grossPriceColumn.setMinWidth(100);
		return grossPriceColumn;
	}

	private static TableColumn<ClientInvoiceTableItem, String> createCommissionRateColumn(TableView<ClientInvoiceTableItem> table) {
		TableColumn<ClientInvoiceTableItem, String> commissionRateColumn = new TableColumn<>("Agentarvode %");
		commissionRateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().commissionRate()));
		commissionRateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		commissionRateColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();

			try {
				double newValue = parsePercentStringToDecimal(event.getNewValue());
				if (newValue < 0 || newValue > 1) {
					log.warn("Invalid commission rate: {}", newValue);
					alert("Felaktigt agentarvode", "Agentarvode måste vara mellan 0 och 100");
				} else {
					item.setCommissionRate(doubleToPercent(newValue));
				}
				table.refresh();
			} catch (NumberFormatException e) {
				log.warn("Failed to parse commission rate", e);
				alert("Felaktig inmatning", "Felaktigt format på agentarvode, använd siffror och punkt eller komma som decimalavskiljare");
				table.refresh();
			}
		});
		commissionRateColumn.setMinWidth(100);
		return commissionRateColumn;
	}

	private static TableColumn<ClientInvoiceTableItem, String> createSerialNumberColumn(TableView<ClientInvoiceTableItem> table) {
		TableColumn<ClientInvoiceTableItem, String> serialNumberColumn = new TableColumn<>("Senaste löpnr");
		serialNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastSerialNumber()));
		serialNumberColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		serialNumberColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();
			String newValue = event.getNewValue();
			if (validateSerialNumber(newValue)) {
				item.setLastSerialNumber(newValue);
			} else {
				log.warn("Invalid serial number: {}", newValue);
				alert("Felaktigt löpnummer", "Löpnummer måste innehålla en bindestreck");
			}
			table.refresh();

		});
		serialNumberColumn.setMinWidth(100);
		return serialNumberColumn;
	}

	private static void alert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private static boolean validateSerialNumber(String input) {
		return input.contains("-");
	}

	public static void repopulateTable(TableView<ClientInvoiceTableItem> table, List<UIData> uiDatas) {
		table.getItems().clear();
		populateTable(table, uiDatas);
		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));
	}

	public static void populateTable(TableView<ClientInvoiceTableItem> table, List<UIData> uiDatas) {
		uiDatas.forEach(data -> {
			log.info("Populating table with data: {}", data);
			ClientInvoice clientInvoice = data.clientInvoice();
			String serialNumberDisplayText = data.serialNumber().map(SerialNumber::displayText).orElse("");
			table.getItems().add(mapClientInvoiceTableItem(clientInvoice, serialNumberDisplayText));
		});
	}

	private static ClientInvoiceTableItem mapClientInvoiceTableItem(ClientInvoice clientInvoice, String serialNumberDisplayText) {

		String commissionRate;
		if (clientInvoice.commissionRate().isEmpty()) {
			commissionRate = "";
		} else {
			commissionRate = bigDecimalToPercent(clientInvoice.commissionRate().get());
		}

		return new ClientInvoiceTableItem(
				clientInvoice.id().getId(),
				clientInvoice.client().name(),
				clientInvoice.invoiceNr(),
				clientInvoice.grossPrice().doubleValue(),
				commissionRate,
				serialNumberDisplayText
		);
	}

}
