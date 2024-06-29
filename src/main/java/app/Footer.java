package app;

import static app.FXMain.uiData;

import java.util.ArrayList;
import java.util.List;

import app.domain.ClientInvoiceTableItem;
import domain.InvoiceId;
import domain.SupplierInvoice;
import domain.SupplierInvoiceRequest;
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

	public Footer(SupplierInvoiceService supplierInvoiceService, PdfCreator pdfCreator) {
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
			SupplierInvoice supplierInvoice = supplierInvoiceService.createSupplierInvoice(
					uiData.get(new InvoiceId(item.id())).clientInvoice(),
					uiData.get(new InvoiceId(item.id())).currentSerialNumber(),
					uiData.get(new InvoiceId(item.id())).supplier(),
					uiData.get(new InvoiceId(item.id())).user()
			);
			SupplierInvoiceRequest.File file = pdfCreator.createPdf(supplierInvoice);
			fileNames.add(file.filename());
			table.getItems().remove(item);
		});
		button.setDisable(false);
		String message = String.join("\n • ", fileNames);
		alert(fileNames.size() + " fakturor skapade", "•  " + message, AlertType.INFORMATION);
	}

	private void alert(String title, String message, AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
