package app;

import static util.BigDecimalUtil.bigDecimalToPercent;
import static util.BigDecimalUtil.doubleToBigDecimal;
import static util.BigDecimalUtil.parsePercentStringToDecimal;

import java.util.Collection;

import app.domain.ClientInvoiceTableItem;
import domain.ClientInvoice;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.TableData;
import exception.GetSupplierException;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Table {

	private final TableDataService tableDataService;
	private final TableView<ClientInvoiceTableItem> table;

	public Table(TableDataService tableDataService) {
		this.tableDataService = tableDataService;
		table = new TableView<>();
	}

	public TableView<ClientInvoiceTableItem> createTable() {
		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		table.getColumns().add(createClientNameColumn());
		table.getColumns().add(createInvoiceNrColumn());
		table.getColumns().add(createGrossPriceColumn());
		table.getColumns().add(createCommissionRateColumn());
		table.getColumns().add(createSupplierNameColumn());
		table.getColumns().add(createSupplierIdTable());
		table.getColumns().add(createSerialNumberColumn());
		table.getColumns().add(createSupplierInvoiceReferenceColumn());

		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));

		return table;
	}

	private TableColumn<ClientInvoiceTableItem, String> createClientNameColumn() {
		TableColumn<ClientInvoiceTableItem, String> clientNameColumn = new TableColumn<>("Kundens namn");
		clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));
		clientNameColumn.setMinWidth(200);
		return clientNameColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createInvoiceNrColumn() {
		TableColumn<ClientInvoiceTableItem, String> invoiceNrColumn = new TableColumn<>("Kundfaktura");
		invoiceNrColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().invoiceNr()));
		invoiceNrColumn.setMinWidth(100);
		return invoiceNrColumn;
	}

	private TableColumn<ClientInvoiceTableItem, Number> createGrossPriceColumn() {
		TableColumn<ClientInvoiceTableItem, Number> grossPriceColumn = new TableColumn<>("Belopp");
		grossPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().grossPrice()));
		grossPriceColumn.setMinWidth(100);
		return grossPriceColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createCommissionRateColumn() {
		TableColumn<ClientInvoiceTableItem, String> commissionRateColumn = new TableColumn<>("Agentarvode %");
		commissionRateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().commissionRate()));
		commissionRateColumn.setCellFactory(column -> new CustomEditableTextFieldTableCell(new DefaultStringConverter()));
		commissionRateColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();

			try {
				double newValue = parsePercentStringToDecimal(event.getNewValue());
				if (newValue < 0 || newValue > 1) {
					log.warn("Invalid commission rate: {}", newValue);
					alert("Felaktigt agentarvode", "Agentarvode måste vara mellan 0 och 100");
				} else {
					tableDataService.addCommissionRate(item, doubleToBigDecimal(newValue));
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

	private TableColumn<ClientInvoiceTableItem, String> createSupplierNameColumn() {
		TableColumn<ClientInvoiceTableItem, String> supplierNameColumn = new TableColumn<>("Klientens namn");
		supplierNameColumn.setCellValueFactory(data -> {
			if (data.getValue().supplier().isEmpty()) {
				return new SimpleStringProperty("");
			}
			return new SimpleStringProperty(data.getValue().supplier().get().name());
		});
		supplierNameColumn.setMinWidth(200);
		return supplierNameColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createSupplierIdTable() {
		TableColumn<ClientInvoiceTableItem, String> supplierIdColumn = new TableColumn<>("KlientId");

		// Set the custom cell factory
		supplierIdColumn.setCellFactory(column -> new CustomEditableTextFieldTableCell(new DefaultStringConverter()));

		supplierIdColumn.setCellValueFactory(data -> {
			if (data.getValue().supplier().isEmpty()) {
				return new SimpleStringProperty("");
			}
			return new SimpleStringProperty(data.getValue().supplier().get().id().getId());
		});

		// Set the on edit commit action
		supplierIdColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();
			String newValue = event.getNewValue();
			try {
				tableDataService.addSupplier(new InvoiceId(item.id()), new SupplierId(newValue), item);
			} catch (GetSupplierException e) {
				log.warn("Failed to get supplier: {}", newValue, e);
				alert("Felaktigt klientId",
						String.format("Lyckades inte hitta klient med KlientId : %s, dubbelkolla att det stämmer", newValue));
			} catch (Exception e) {
				log.warn("Failed to get serial number for supplier: {}", newValue, e);
				alert("Felaktigt klientId",
						String.format("Lyckades inte hitta löpnummer för klient med KlientId : %s", newValue));
			}
			table.refresh();
		});

		supplierIdColumn.setMinWidth(100);
		return supplierIdColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createSerialNumberColumn() {
		TableColumn<ClientInvoiceTableItem, String> serialNumberColumn = new TableColumn<>("Senaste löpnr");
		serialNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastSerialNumber()));
		serialNumberColumn.setMinWidth(100);
		return serialNumberColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createSupplierInvoiceReferenceColumn() {
		TableColumn<ClientInvoiceTableItem, String> supplierInvoiceReferenceColumn = new TableColumn<>("Självfaktura");
		supplierInvoiceReferenceColumn.setCellValueFactory(data -> {
			if (data.getValue().supplierInvoiceReference().isEmpty()) {
				return new SimpleStringProperty("");
			}
			return new SimpleStringProperty(data.getValue().supplierInvoiceReference().get());
		});
		supplierInvoiceReferenceColumn.setMinWidth(150);
		return supplierInvoiceReferenceColumn;
	}

	private void alert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void populateTable() {
		Collection<TableData> tableDatas = tableDataService.fetchUIData().values();
		refreshTable(tableDatas);
	}

	private void refreshTable(Collection<TableData> tableDatas) {
		table.getItems().clear();
		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));
		tableDatas.forEach(data -> {
			log.info("Populating table with data: {}", data);
			ClientInvoice clientInvoice = data.clientInvoice();
			String supplierInvoiceReference = tableDataService.getSupplierInvoiceReference(clientInvoice.id());
			String fullSerialNumber = data.serialNumber().map(SerialNumber::fullSerialNumber).orElse("");
			table.getItems().add(mapClientInvoiceTableItem(clientInvoice, fullSerialNumber, data.supplier().orElse(null), supplierInvoiceReference));
		});
	}

	private static ClientInvoiceTableItem mapClientInvoiceTableItem(ClientInvoice clientInvoice,
			String fullSerialNumber,
			Supplier supplier,
			String supplierInvoiceReference) {

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
				fullSerialNumber,
				supplier,
				supplierInvoiceReference
		);
	}

	private static class CustomEditableTextFieldTableCell extends TextFieldTableCell<ClientInvoiceTableItem, String> {
		public CustomEditableTextFieldTableCell(StringConverter<String> converter) {
			super(converter);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (item == null || empty) {
				setText(null);
				setStyle("");
			} else {
				setText(item);
				// Apply the red background if the item's value is an empty string
				if (item.isEmpty()) {
					setStyle("-fx-background-color: pink;");
				} else {
					setStyle("");
				}
			}
		}
	}
}


