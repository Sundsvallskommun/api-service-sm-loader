package se.sundsvall.smloader.integration.openeinternalsoap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e.internal-soap")
public record OpenEInternalSoapProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout
) {

}
