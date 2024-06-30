package app;

import static app.Table.createTable;
import static app.Table.populateTable;

import java.util.Map;

import app.domain.ClientInvoiceTableItem;
import domain.InvoiceId;
import domain.UIData;
import facade.ClientFacade;
import facade.ClientInvoiceFacade;
import facade.PEHttpClient;
import facade.SupplierFacade;
import facade.SupplierInvoiceFacade;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import service.ClientInvoiceService;
import service.SerialNumberService;
import service.SupplierInvoiceService;
import service.SupplierService;
import service.UIDataService;
import service.UserService;
import util.PdfCreator;

@Slf4j
public class FXMain extends Application {
	private static final PEHttpClient peHttpClient;
	private static final ClientInvoiceFacade clientInvoiceFacade;
	private static final ClientFacade clientFacade;
	private static final UserService userService;
	private static final ClientInvoiceService clientInvoiceService;
	private static final SupplierFacade supplierFacade;
	private static final SupplierService supplierService;
	private static final SerialNumberService serialNumberService;
	private static final SupplierInvoiceFacade supplierInvoiceFacade;
	private static final SupplierInvoiceService supplierInvoiceService;
	private static final PdfCreator pdfCreator;
	private static final UIDataService uiDataService;
	private static final Header header;
	private static final Footer footer;
	public static Map<InvoiceId, UIData> uiDataMap;

	static {
		peHttpClient = new PEHttpClient();
		clientInvoiceFacade = new ClientInvoiceFacade(peHttpClient);
		clientFacade = new ClientFacade(peHttpClient);
		userService = new UserService(peHttpClient);
		clientInvoiceService = new ClientInvoiceService(clientInvoiceFacade, clientFacade);
		supplierFacade = new SupplierFacade(peHttpClient);
		supplierService = new SupplierService(supplierFacade);
		supplierInvoiceFacade = new SupplierInvoiceFacade(peHttpClient);
		serialNumberService = new SerialNumberService(supplierInvoiceFacade);
		pdfCreator = new PdfCreator();
		supplierInvoiceService = new SupplierInvoiceService();
		uiDataService = new UIDataService(clientInvoiceService, userService, supplierService, serialNumberService);
		header = new Header(uiDataService);
		footer = new Footer(supplierInvoiceService, pdfCreator);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		uiDataMap = uiDataService.fetchUIData();

		TableView<ClientInvoiceTableItem> table = createTable();

		populateTable(table, uiDataMap.values().stream().toList());

		Button createInvoiceButton = footer.addCreateInvoiceButton(table);

		VBox inputField = header.getHeader(table);
		VBox vbox = new VBox(inputField, table, createInvoiceButton);

		Scene scene = new Scene(vbox);
		primaryStage.setTitle("Alba");
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();

	}

}