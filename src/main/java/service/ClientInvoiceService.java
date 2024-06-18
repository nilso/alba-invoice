package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import config.Config;
import domain.Client;
import domain.ClientInvoice;
import domain.ClientInvoiceResponse;
import domain.ClientResponse;
import domain.Field;
import domain.ProductRow;
import domain.SupplierId;
import facade.ClientFacade;
import facade.ClientInvoiceFacade;
import lombok.extern.slf4j.Slf4j;
import util.BigDecimalUtil;

@Slf4j
public class ClientInvoiceService {
	private final ClientInvoiceFacade clientInvoiceFacade;
	private final ClientFacade clientFacade;

	public ClientInvoiceService(ClientInvoiceFacade clientInvoiceFacade, ClientFacade clientFacade) {
		this.clientInvoiceFacade = clientInvoiceFacade;
		this.clientFacade = clientFacade;
	}

	public List<ClientInvoice> getUnprocessedClientInvoices() throws Exception {
		List<ClientInvoiceResponse> clientInvoiceResponses = clientInvoiceFacade.fetchClientInvoices(Config.getDaysBack());

		if (clientInvoiceResponses.isEmpty()) {
			throw new RuntimeException("No client invoices found");
		}

		//TODO filtrera allt som har länkar till leverantörsfakturor.

		List<ClientInvoice> clientInvoices = clientInvoiceResponses.stream()
				.filter(response -> findCommission(response).isPresent())
				.filter(response -> findSupplierId(response).isPresent())
				.filter(ClientInvoiceResponse::certified)
				.map(clientInvoiceResponse -> {
					try {
						ClientResponse clientResponse = clientFacade.fetchClientById(clientInvoiceResponse.clientId());
						return mapClientInvoice(clientInvoiceResponse, clientResponse);
					} catch (Exception e) {
						throw new RuntimeException("Failed to map client invoice: " + e);
					}
				})
				.toList();

		log.info("Client invoices fetched: {}", clientInvoices);
		return clientInvoices;
	}

	private static Optional<Field> findCommission(ClientInvoiceResponse response) {
		return response.fields().fields().stream()
				.filter(field -> field.alias().equals("agentarvode"))
				.filter(field -> field.value() != null && !field.value().isEmpty())
				.findFirst();
	}

	private static Optional<Field> findSupplierId(ClientInvoiceResponse response) {
		return response.fields().fields().stream()
				.filter(field -> field.alias().equals("klientID"))
				.filter(field -> field.value() != null && !field.value().isEmpty())
				.findFirst();
	}

	private ClientInvoice mapClientInvoice(ClientInvoiceResponse clientInvoiceResponse, ClientResponse clientResponse) throws ParseException {
		Field agentarvodeField = clientInvoiceResponse.fields().fields().stream()
				.filter(field -> field.alias().equals("agentarvode"))
				.findFirst().orElseThrow(() -> new RuntimeException("No agentarvode found"));
		Field klientIDField = clientInvoiceResponse.fields().fields().stream()
				.filter(field -> field.alias().equals("klientID"))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No supplier ID found"));

		BigDecimal commissionRate = parsePercentage(agentarvodeField.value());
		BigDecimal netPrice = zeroPaddedDoubleToBigDecimal(clientInvoiceResponse.amount());
		BigDecimal vatAmount = zeroPaddedDoubleToBigDecimal(clientInvoiceResponse.vat());
		BigDecimal grossPrice = netPrice.add(vatAmount);
		Optional<BigDecimal> roundingAmount = BigDecimalUtil.extractDecimalPartIfNotZero(grossPrice);
		grossPrice = BigDecimalUtil.roundToWholeNumberButKeepTwoDecimals(grossPrice);

		List<ProductRow> productRows = mapRows(clientInvoiceResponse);

		Client client = mapClient(clientResponse);

		return new ClientInvoice(
				clientInvoiceResponse.id(),
				client,
				clientInvoiceResponse.invoiceNr(),
				clientInvoiceResponse.ourReference(),
				clientInvoiceResponse.yourReference(),
				clientInvoiceResponse.invoiceAddress(),
				productRows,
				clientInvoiceResponse.invoiceDate(),
				clientInvoiceResponse.dueDate(),
				grossPrice,
				roundingAmount,
				netPrice,
				vatAmount,
				clientInvoiceResponse.currency(),
				commissionRate,
				new SupplierId(klientIDField.value()));
	}

	private static BigDecimal parsePercentage(String percentageString) throws ParseException {
		if (percentageString == null || percentageString.isEmpty()) {
			throw new IllegalArgumentException("Percentage string cannot be null or empty.");
		}

		// Remove the '%' character
		String numericPart = percentageString.replace("%", "");

		// Use NumberFormat to parse the string
		Number number = NumberFormat.getNumberInstance().parse(numericPart);

		BigDecimal bigDecimal = BigDecimal.valueOf(number.doubleValue()).setScale(4, RoundingMode.HALF_UP);

		// Convert the number to a decimal representation of the percentage
		return bigDecimal.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
	}

	private static List<ProductRow> mapRows(ClientInvoiceResponse clientInvoiceResponse) {
		return clientInvoiceResponse.rows().rows().stream()
				.map(row -> {
					BigDecimal price = zeroPaddedDoubleToBigDecimal(row.price());
					BigDecimal vatRate = doubleToBigDecimal(row.vatRate());
					return new ProductRow(price, vatRate, row.product(), row.description());
				})
				.toList();
	}

	private static Client mapClient(ClientResponse clientResponse) {
		return new Client(clientResponse.id(),
				clientResponse.name(),
				clientResponse.vatNumber(),
				clientResponse.orgNo(),
				clientResponse.countryCode());
	}

	private static BigDecimal zeroPaddedDoubleToBigDecimal(double value) {
		return BigDecimal.valueOf(value)
				.setScale(2, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

	}

	private static BigDecimal doubleToBigDecimal(double value) {
		return BigDecimal.valueOf(value)
				.setScale(2, RoundingMode.HALF_UP);
	}
}
