package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import domain.Client;
import domain.ClientInvoice;
import domain.ClientInvoiceResponse;
import domain.ClientResponse;
import domain.Field;
import domain.ProductRow;
import domain.SerialNumber;
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

	public List<ClientInvoice> getUnprocessedClientInvoices(int daysBack) throws Exception {
		List<ClientInvoiceResponse> clientInvoiceResponses = clientInvoiceFacade.fetchClientInvoices(daysBack);

		if (clientInvoiceResponses == null || clientInvoiceResponses.isEmpty()) {
			return List.of();
		}

		//TODO filtrera allt som har länkar till leverantörsfakturor.

		List<ClientInvoice> clientInvoices = clientInvoiceResponses.stream()
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

	private ClientInvoice mapClientInvoice(ClientInvoiceResponse clientInvoiceResponse, ClientResponse clientResponse) throws ParseException {
		Field agentarvodeField = clientInvoiceResponse.fields().fields().stream()
				.filter(field -> field.alias().equals("agentarvode"))
				.findFirst().orElseThrow(() -> new RuntimeException("No agentarvode found"));
		Field klientIDField = clientInvoiceResponse.fields().fields().stream()
				.filter(field -> field.alias().equals("klientID"))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No supplier ID found"));

		Optional<SupplierId> supplierId = parseSupplierId(klientIDField.value());
		Optional<BigDecimal> commissionRate = parsePercentage(agentarvodeField.value());
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
				supplierId);
	}

	private Optional<SupplierId> parseSupplierId(String customFieldString) {
		if (customFieldString == null || customFieldString.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new SupplierId(customFieldString));
	}

	private static Optional<BigDecimal> parsePercentage(String percentageString) throws ParseException {
		if (percentageString == null || percentageString.isEmpty()) {
			return Optional.empty();
		}

		// Remove the '%' character
		String numericPart = percentageString.replace("%", "");

		// Use NumberFormat to parse the string
		Number number = NumberFormat.getNumberInstance().parse(numericPart);

		BigDecimal bigDecimal = BigDecimal.valueOf(number.doubleValue()).setScale(4, RoundingMode.HALF_UP);

		// Convert the number to a decimal representation of the percentage
		return Optional.of(bigDecimal.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
	}

	private Optional<String> parseSerialNumber(String customFieldString) {
		if (customFieldString == null || customFieldString.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(customFieldString);
	}

	private static BigDecimal zeroPaddedDoubleToBigDecimal(double value) {
		return BigDecimal.valueOf(value)
				.setScale(2, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

	}

	private static List<ProductRow> mapRows(ClientInvoiceResponse clientInvoiceResponse) {
		return clientInvoiceResponse.rows().rows().stream()
				.map(row -> {
					BigDecimal price = zeroPaddedDoubleToBigDecimal(row.price());
					BigDecimal vatRate = BigDecimalUtil.doubleToBigDecimal(row.vatRate());
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
}
