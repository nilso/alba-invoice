package app;

import static util.BigDecimalUtil.bigDecimalToPercent;
import static util.BigDecimalUtil.doubleToPercent;
import static util.BigDecimalUtil.parsePercentStringToDecimal;

import java.util.List;

import app.domain.ClientInvoiceTableItem;
import domain.ClientInvoice;
import domain.SerialNumber;
import domain.Supplier;
import domain.SupplierId;
import domain.UIData;
import exception.GetSupplierException;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.slf4j.Slf4j;
import service.SerialNumberService;
import service.SupplierService;

@Slf4j
public class Table {

	private final SupplierService supplierService;
	private final SerialNumberService serialNumberService;

	public Table(SupplierService supplierService,
			SerialNumberService serialNumberService) {
		this.supplierService = supplierService;
		this.serialNumberService = serialNumberService;
	}
	public TableView<ClientInvoiceTableItem> createTable() {
		TableView<ClientInvoiceTableItem> table = new TableView<>();

		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		table.getColumns().add(createClientNameColumn());
		table.getColumns().add(createInvoiceNrColumn());
		table.getColumns().add(createGrossPriceColumn());
		table.getColumns().add(createCommissionRateColumn(table));
		table.getColumns().add(createSupplierNameColumn());
		table.getColumns().add(createSupplierIdTable(table));
		table.getColumns().add(createSerialNumberColumn(table));

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

	private TableColumn<ClientInvoiceTableItem, String> createCommissionRateColumn(TableView<ClientInvoiceTableItem> table) {
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

	private TableColumn<ClientInvoiceTableItem, String> createSupplierNameColumn() {
		TableColumn<ClientInvoiceTableItem, String> supplierNameColumn = new TableColumn<>("Klientens namn");
		supplierNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().supplierName()));
		supplierNameColumn.setMinWidth(200);
		return supplierNameColumn;
	}

	private TableColumn<ClientInvoiceTableItem, String> createSerialNumberColumn(TableView<ClientInvoiceTableItem> table) {
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

	private void alert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private boolean validateSerialNumber(String input) {
		return input.contains("-");
	}

	private TableColumn<ClientInvoiceTableItem, String> createSupplierIdTable(TableView<ClientInvoiceTableItem> table) {
		TableColumn<ClientInvoiceTableItem, String> supplierIdColumn = new TableColumn<>("KlientId");
		supplierIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().supplierId()));
		supplierIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		supplierIdColumn.setOnEditCommit(event -> {
			ClientInvoiceTableItem item = event.getRowValue();
			String newValue = event.getNewValue();
			try {
				Supplier supplier = supplierService.getSupplier(new SupplierId(newValue));
				SerialNumber serialNumber = serialNumberService.getCurrentSerialOrNewIfNone(supplier);
				item.setSupplierName(supplier.name());
				item.setSupplierId(supplier.id().getId());
				item.setLastSerialNumber(serialNumber.fullSerialNumber());
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

	public static void repopulateTable(TableView<ClientInvoiceTableItem> table, List<UIData> uiDatas) {
		table.getItems().clear();
		populateTable(table, uiDatas);
		table.setPlaceholder(new Label("Inga fakturor att visa för perioden"));
	}

	public static void populateTable(TableView<ClientInvoiceTableItem> table, List<UIData> uiDatas) {
		uiDatas.forEach(data -> {
			log.info("Populating table with data: {}", data);
			ClientInvoice clientInvoice = data.clientInvoice();
			String fullSerialNumber = data.serialNumber().map(SerialNumber::fullSerialNumber).orElse("");
			String supplierName = data.supplier().map(Supplier::name).orElse("");
			String supplierId = data.supplier().map(supplier -> supplier.id().getId()).orElse("");

			table.getItems().add(mapClientInvoiceTableItem(clientInvoice, fullSerialNumber, supplierName, supplierId));
		});
	}

	private static ClientInvoiceTableItem mapClientInvoiceTableItem(ClientInvoice clientInvoice,
			String fullSerialNumber,
			String supplierName,
			String supplierId) {

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
				supplierName,
				supplierId
		);
	}

}
