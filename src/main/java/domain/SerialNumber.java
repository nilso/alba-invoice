package domain;

public record SerialNumber(String prefix, Integer suffix) {
	public SerialNumber incrementSuffix(int steps) {
		return new SerialNumber(prefix, suffix + steps);
	}

	public String fullSerialNumber() {
		return prefix + "-" + suffix;
	}
}