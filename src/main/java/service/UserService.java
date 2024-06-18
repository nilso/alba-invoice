package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.ClientInvoice;
import domain.InvoiceId;
import domain.User;
import facade.PEHttpClient;
import facade.UserFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserService {
	private final PEHttpClient peHttpClient;

	public UserService(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
	}

	public Map<InvoiceId, User> getUserMap(List<ClientInvoice> invoices) {
		UserFacade userFacade = new UserFacade(peHttpClient);
		Map<InvoiceId, User> userMap = new HashMap<>();
		invoices.forEach(invoice -> {
			try {
				userMap.put(invoice.id(), userFacade.fetchUserName(invoice.ourReferenceId().id()));
			} catch (Exception e) {
				log.error("Failed to fetch user: " + e);
			}
		});

		return userMap;
	}

}
