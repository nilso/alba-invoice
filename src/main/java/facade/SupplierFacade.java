package facade;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;
import domain.SupplierId;
import domain.SupplierResponse;
import domain.SuppliersResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierFacade {
	private final PEHttpClient peHttpClient;

	public SupplierFacade(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
	}

	public SupplierResponse fetchSupplier(SupplierId id) throws Exception {
		String endpoint = String.format("/company/%s/supplier/%s", Config.getClientId(), id.getId());

		String response = peHttpClient.httpGet(endpoint);
		log.info(response);
		ObjectMapper objectMapper = new ObjectMapper();
		SupplierResponse supplier = objectMapper.readValue(response, SupplierResponse.class);
		log.info("Supplier fetched: {}", supplier);
		return supplier;
	}

	public List<SupplierResponse> fetchAllSuppliers() throws Exception {
		String endpoint = String.format("/company/%s/supplier/", Config.getClientId());

		String response = peHttpClient.httpGet(endpoint);
		log.info(response);
		ObjectMapper objectMapper = new ObjectMapper();
		SuppliersResponse suppliersResponse = objectMapper.readValue(response, SuppliersResponse.class);
		List<SupplierResponse> suppliers = suppliersResponse.suppliers();
		log.info("Suppliers fetched: {}", suppliers);
		return suppliers;
	}
}
