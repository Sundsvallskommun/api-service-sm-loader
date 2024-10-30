package se.sundsvall.smloader.integration.openeexternalsoap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e.external-soap")
public record OpenEExternalSoapProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout) {

}
