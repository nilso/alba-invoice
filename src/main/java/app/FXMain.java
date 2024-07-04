package app;

import app.domain.ClientInvoiceTableItem;
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
	private static final TableDataService tableDataService;
	private static final Header header;
	private static final Table table;
	private static final Footer footer;

	static {
		peHttpClient = new PEHttpClient();
		clientInvoiceFacade = new ClientInvoiceFacade(peHttpClient);
		clientFacade = new ClientFacade(peHttpClient);
		userService = new UserService(peHttpClient);
		clientInvoiceService = new ClientInvoiceService(clientInvoiceFacade, clientFacade);
		supplierFacade = new SupplierFacade(peHttpClient);
		supplierService = new SupplierService(supplierFacade);
		supplierInvoiceFacade = new SupplierInvoiceFacade(peHttpClient);
		serialNumberService = new SerialNumberService(supplierInvoiceFacade, supplierService);
		pdfCreator = new PdfCreator();
		supplierInvoiceService = new SupplierInvoiceService();
		tableDataService = new TableDataService(clientInvoiceService, userService, supplierService, serialNumberService, supplierInvoiceFacade);
		footer = new Footer(supplierInvoiceService, pdfCreator, tableDataService);
		table = new Table(tableDataService);
		header = new Header(tableDataService, table);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		tableDataService.init();

		TableView<ClientInvoiceTableItem> tableView = table.createTable();

		table.populateTable();

		Button createInvoiceButton = footer.addCreateInvoiceButton(tableView);

		VBox inputField = header.getHeader(tableView);
		VBox vbox = new VBox(inputField, tableView, createInvoiceButton);

		Scene scene = new Scene(vbox);
		primaryStage.setTitle("AlbaInvoice");
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();

	}

}