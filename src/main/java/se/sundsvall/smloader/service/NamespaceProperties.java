package se.sundsvall.smloader.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties()
public class NamespaceProperties {

	private Map<String, List<String>> namespace;

	public Map<String, List<String>> getNamespace() {
		return namespace;
	}

	public void setNamespace(Map<String, List<String>> namespace) {
		this.namespace = namespace;
	}
}
