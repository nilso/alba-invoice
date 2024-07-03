package app;

import static app.FXMain.uiDataMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import app.domain.ClientInvoiceTableItem;
import domain.InvoiceId;
import domain.SerialNumber;
import domain.SupplierInvoice;
import domain.SupplierInvoiceRequest;
import domain.UIData;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import service.SupplierInvoiceService;
import util.PdfCreator;

@Slf4j
public class Footer {

	private final SupplierInvoiceService supplierInvoiceService;
	private final PdfCreator pdfCreator;

	public Footer(SupplierInvoiceService supplierInvoiceService,
			PdfCreator pdfCreator) {
		this.supplierInvoiceService = supplierInvoiceService;
		this.pdfCreator = pdfCreator;
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
		log.info("Button clicked for items: {}", items);
		button.setDisable(true);
		List<String> fileNames = new ArrayList<>();
		items.forEach(item -> {
			log.info("Creating invoice for item: {}", item);

			UIData uiData = uiDataMap.get(new InvoiceId(item.id()));
			if (item.lastSerialNumber().isEmpty()) {
				log.warn("Could not find serial number for item: {}", item);
				alert("Löpnr saknas", "Löpnr måste vara satt", AlertType.ERROR);
			} else if (item.commissionRate().isEmpty()) {
				log.warn("Could not find commission rate for item: {}", item);
				alert("Agentarvode saknas", "Agentarvode måste vara satt", AlertType.ERROR);
			} else if (item.supplierId().isEmpty()) {
				log.warn("Could not find supplier for item: {}", item);
				alert("KlientId saknas", "KlientId måste vara satt", AlertType.ERROR);
			} else {
				BigDecimal commissionRate = new BigDecimal(item.commissionRate());
				SerialNumber currentSerialNumber = serialNumberFromTable(item.lastSerialNumber());
				SupplierInvoice supplierInvoice = supplierInvoiceService.createSupplierInvoice(
						uiData.clientInvoice().withUITableData(commissionRate),
						currentSerialNumber,
						uiData.supplier().get(),
						uiData.user()
				);
				SupplierInvoiceRequest.File file = pdfCreator.createPdf(supplierInvoice);
				fileNames.add(file.filename());
				table.getItems().remove(item);
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
		alert.setContentText(message);
		alert.showAndWait();
	}

	public SerialNumber serialNumberFromTable(String input) {
		if (input == null || !input.contains("-")) {
			throw new IllegalArgumentException("Input must contain a hyphen (-)");
		}

		String[] parts = input.split("-");
		return new SerialNumber(parts[0], Integer.parseInt(parts[1]));
	}

}
