package se.sundsvall.smloader.integration.openeinternal.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e.internal")
public record OpenEInternalProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout) {

}
