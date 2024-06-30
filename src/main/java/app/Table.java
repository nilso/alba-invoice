package app;

import java.util.List;
import java.util.Map;

import app.domain.ClientInvoiceTableItem;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.UIData;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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

		TableColumn<ClientInvoiceTableItem, String> idColumn = new TableColumn<>("Id");
		idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().id()));
		idColumn.setMinWidth(100);

		TableColumn<ClientInvoiceTableItem, String> clientNameColumn = new TableColumn<>("Fakturamottagare");
		clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));
		clientNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		clientNameColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();
			item.setClientName(event.getNewValue());
		});
		clientNameColumn.setMinWidth(200);

		TableColumn<ClientInvoiceTableItem, String> invoiceNrColumn = new TableColumn<>("Fakturanummer");
		invoiceNrColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().invoiceNr()));
		invoiceNrColumn.setMinWidth(100);

		TableColumn<ClientInvoiceTableItem, Number> grossPriceColumn = new TableColumn<>("Belopp inkl. moms");
		grossPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().grossPrice()));
		grossPriceColumn.setMinWidth(200);

		TableColumn<ClientInvoiceTableItem, Number> commissionRateColumn = new TableColumn<>("Agentarvode");
		commissionRateColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().commissionRate()));
		commissionRateColumn.setMinWidth(100);

		table.getColumns().add(idColumn);
		table.getColumns().add(clientNameColumn);
		table.getColumns().add(invoiceNrColumn);
		table.getColumns().add(grossPriceColumn);
		table.getColumns().add(commissionRateColumn);

		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));

		return table;
	}

	public static void refreshTable(TableView<ClientInvoiceTableItem> table, Map<InvoiceId, UIData> uiData) {
		table.getItems().clear();
		populateTable(table, uiData);
		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));
	}

	public static void populateTable(TableView<ClientInvoiceTableItem> table, Map<InvoiceId, UIData> uiData) {
		List<ClientInvoice> clientInvoices = uiData.values().stream()
				.map(UIData::clientInvoice).toList();

		clientInvoices.stream()
				.map(Table::mapClientInvoiceTableItem)
				.forEach(table.getItems()::add);
	}

	private static ClientInvoiceTableItem mapClientInvoiceTableItem(ClientInvoice clientInvoice) {
		return new ClientInvoiceTableItem(
				clientInvoice.id().getId(),
				clientInvoice.client().name(),
				clientInvoice.invoiceNr(),
				clientInvoice.grossPrice().doubleValue(),
				clientInvoice.commissionRate().doubleValue()
		);
	}
}
