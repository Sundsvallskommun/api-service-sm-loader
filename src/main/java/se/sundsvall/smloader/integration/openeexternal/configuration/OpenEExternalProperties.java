package se.sundsvall.smloader.integration.openeexternal.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e.external")
public record OpenEExternalProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout
) {

}
