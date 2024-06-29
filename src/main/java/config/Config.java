package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static final Properties properties = new Properties();

	static {
		try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				throw new IOException("Unable to find config.properties");
			}
			properties.load(input);
		} catch (IOException ex) {
			throw new ExceptionInInitializerError("Failed to load config.properties: " + ex);
		}
	}

	public static String getBaseUrl() {
		return properties.getProperty("BASE_URL");
	}

	public static String getApiKey() {
		return properties.getProperty("API_KEY");
	}

	public static String getClientId() {
		return properties.getProperty("CLIENT_ID");
	}

	public static int getDefaultDaysBack() {
		return Integer.parseInt(properties.getProperty("DEFAULT_DAYS_BACK"));
	}
}