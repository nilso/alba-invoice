package facade;

import static config.config.CLIENT_ID;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		String endpoint = String.format("/company/%s/supplier/%s", CLIENT_ID, id.getId());

		String response = peHttpClient.httpCall(endpoint);
		log.info(response);
		ObjectMapper objectMapper = new ObjectMapper();
		SupplierResponse supplier = objectMapper.readValue(response, SupplierResponse.class);
		log.info("Supplier fetched: {}", supplier);
		return supplier;
	}

	public List<SupplierResponse> fetchAllSuppliers() throws Exception {
		String endpoint = String.format("/company/%s/supplier/", CLIENT_ID);

		String response = peHttpClient.httpCall(endpoint);
		log.info(response);
		ObjectMapper objectMapper = new ObjectMapper();
		SuppliersResponse suppliersResponse = objectMapper.readValue(response, SuppliersResponse.class);
		List<SupplierResponse> suppliers = suppliersResponse.getSuppliers();
		log.info("Suppliers fetched: {}", suppliers);
		return suppliers;
	}
}
