module alba.invoice {
	requires javafx.controls;
	requires javafx.fxml;
	requires static lombok;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires org.slf4j;
	requires org.apache.commons.io;
	requires java.net.http;
	requires org.apache.poi.poi;
	requires org.apache.poi.ooxml;
	requires org.apache.pdfbox;

	opens app to javafx.fxml;
	exports app;
	exports app.domain;
	opens domain to com.fasterxml.jackson.databind;
	exports service;
	exports util;
	opens service to javafx.fxml;
	exports domain;
}