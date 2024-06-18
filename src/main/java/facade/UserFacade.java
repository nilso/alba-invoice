package facade;

import static config.config.CLIENT_ID;

import com.fasterxml.jackson.databind.ObjectMapper;

import domain.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserFacade {

	private final PEHttpClient peHttpClient;

	public UserFacade(PEHttpClient peHttpClient) {
		this.peHttpClient = peHttpClient;
	}
	public User fetchUserName(int id) throws Exception {
		String endpoint = String.format("/company/%s/user/%s", CLIENT_ID, id);

		String response = peHttpClient.httpCall(endpoint);

		ObjectMapper objectMapper = new ObjectMapper();
		User user = objectMapper.readValue(response, User.class);
		log.info("User fetched: {}", user);

		if (user.name() == null) {
			throw new RuntimeException(("User not found"));
		}

		return user;
	}
}
