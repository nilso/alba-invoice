package app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.domain.ClientInvoiceTableItem;
import domain.InvoiceId;
import domain.SupplierInvoiceData;
import domain.SupplierInvoiceRequest;
import domain.TableData;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import service.SupplierInvoiceService;
import util.PdfCreator;

@Slf4j
public class Footer {

	private final SupplierInvoiceService supplierInvoiceService;
	private final PdfCreator pdfCreator;
	private final TableDataService tableDataService;

	public Footer(SupplierInvoiceService supplierInvoiceService,
			PdfCreator pdfCreator,
			TableDataService tableDataService) {
		this.supplierInvoiceService = supplierInvoiceService;
		this.pdfCreator = pdfCreator;
		this.tableDataService = tableDataService;
	}

	public Button addCreateInvoiceButton(TableView<ClientInvoiceTableItem> table) {
		Button button = new Button("Skapa fakturor");
		button.setOnAction(event -> {
			ObservableList<ClientInvoiceTableItem> selectedItems = table.getSelectionModel().getSelectedItems();
			if (selectedItems != null) {
				createInvoice(selectedItems, button, table);
			} else {
				alert("Fel", "Ingen rad vald, markera en först", AlertType.ERROR);
				log.info("No row selected");
			}
		});

		table.getSelectionModel().getSelectedItems()
				.addListener((Change<? extends ClientInvoiceTableItem> change) -> {
					int selectedCount = table.getSelectionModel().getSelectedItems().size();
					if (selectedCount > 0) {
						button.setText("Skapa fakturor (" + selectedCount + ")");
					} else {
						button.setText("Skapa fakturor");
					}
				});
		return button;
	}

	private void createInvoice(ObservableList<ClientInvoiceTableItem> incomingItems,
			Button button,
			TableView<ClientInvoiceTableItem> table) {
		List<ClientInvoiceTableItem> items = new ArrayList<>(incomingItems);
		items.sort(Comparator.comparing(ClientInvoiceTableItem::invoiceNr));
		log.info("Button clicked for items: {}", items);
		button.setDisable(true);
		List<String> fileNames = new ArrayList<>();
		items.forEach(item -> {
			log.info("Creating invoice for item: {}", item);
			TableData tableData;
			try {
				tableData = tableDataService.fetchUIData().get(new InvoiceId(item.id()));
				if (tableData.serialNumber().isEmpty()) {
					log.warn("Could not find serial number for item: {}", item);
					alert("Löpnr saknas", "Löpnr måste vara satt", AlertType.ERROR);
				} else if (tableData.clientInvoice().commissionRate().isEmpty()) {
					log.warn("Could not find commission rate for item: {}", item);
					alert("Agentarvode saknas", "Agentarvode måste vara satt", AlertType.ERROR);
				} else if (tableData.supplier().isEmpty()) {
					log.warn("Could not find supplier for item: {}", item);
					alert("KlientId saknas", "KlientId måste vara satt", AlertType.ERROR);
				} else {
					SupplierInvoiceData supplierInvoiceData = supplierInvoiceService.createSupplierInvoiceData(
							tableData.clientInvoice(),
							tableData.serialNumber().get(),
							tableData.supplier().get(),
							tableData.user()
					);
					SupplierInvoiceRequest.File file = pdfCreator.createPdf(supplierInvoiceData);
					fileNames.add(file.filePath());
					table.getItems().remove(item);
				}
			} catch (Exception e) {
				log.error("Failed to fetch UIData for item: {}", item, e);
				alert("Fel", "Kunde inte hämta data för faktura", AlertType.ERROR);
			}
		});

		if (!fileNames.isEmpty()) {
			String message = String.join("\n • ", fileNames);
			alert(fileNames.size() + " fakturor skapade", "•  " + message, AlertType.INFORMATION);
		}

		button.setDisable(false);
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
