package app;

import java.util.List;

import domain.ClientInvoice;
import facade.ClientFacade;
import facade.ClientInvoiceFacade;
import facade.PEHttpClient;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import service.ClientInvoiceService;

@Slf4j
public class FXMain extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		PEHttpClient peHttpClient = new PEHttpClient();
		ClientInvoiceFacade clientInvoiceFacade = new ClientInvoiceFacade(peHttpClient);
		ClientFacade clientFacade = new ClientFacade(peHttpClient);
		ClientInvoiceService clientInvoiceService = new ClientInvoiceService(clientInvoiceFacade, clientFacade);

		TableView<ClientInvoiceTableItem> table = createTable();

		List<ClientInvoice> invoices = clientInvoiceService.getUnprocessedClientInvoices();
		invoices.stream()
				.map(this::mapClientInvoiceTableItem)
				.forEach(table.getItems()::add);

		VBox vbox = new VBox(table);

		Scene scene = new Scene(vbox);
		primaryStage.setTitle("Alba");
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private TableView<ClientInvoiceTableItem> createTable() {
		TableView<ClientInvoiceTableItem> table = new TableView<>();

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<ClientInvoiceTableItem, String> idColumn = new TableColumn<>("Id");
		idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().id()));

		TableColumn<ClientInvoiceTableItem, String> clientNameColumn = new TableColumn<>("ClientName");
		clientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));

		TableColumn<ClientInvoiceTableItem, String> invoiceNrColumn = new TableColumn<>("invoiceNr");
		invoiceNrColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().invoiceNr()));

		TableColumn<ClientInvoiceTableItem, Number> grossPriceColumn = new TableColumn<>("grossPrice");
		grossPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().grossPrice()));

		TableColumn<ClientInvoiceTableItem, Number> commissionRateColumn = new TableColumn<>("commissionRate");
		commissionRateColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().commissionRate()));

		table.getColumns().add(idColumn);
		table.getColumns().add(clientNameColumn);
		table.getColumns().add(invoiceNrColumn);
		table.getColumns().add(grossPriceColumn);
		table.getColumns().add(commissionRateColumn);

		addButton(table);

		return table;
	}

	private ClientInvoiceTableItem mapClientInvoiceTableItem(ClientInvoice clientInvoice) {
		return new ClientInvoiceTableItem(
				clientInvoice.id().getId(),
				clientInvoice.client().name(),
				clientInvoice.invoiceNr(),
				clientInvoice.grossPrice().doubleValue(),
				clientInvoice.commissionRate().doubleValue()
		);
	}

	private TableView<ClientInvoiceTableItem> addButton(TableView<ClientInvoiceTableItem> table) {
		TableColumn<ClientInvoiceTableItem, Void> buttonColumn = new TableColumn<>("Action");
		buttonColumn.setCellFactory(param -> new TableCell<>() {
			private final Button btn = new Button("Action");

			{
				btn.setOnAction(event -> {
					ClientInvoiceTableItem item = getTableView().getItems().get(getIndex());
					performAction(item);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(btn);
				}
			}
		});

		table.getColumns().add(buttonColumn);

		return table;
	}

	private void performAction(ClientInvoiceTableItem item) {
		// This is the function that gets called when the button is clicked.
		// You can put your own logic here.
		System.out.println("Button clicked for item: " + item);
	}

	private record ClientInvoiceTableItem(String id, String clientName, String invoiceNr, double grossPrice, double commissionRate) {
	}
}