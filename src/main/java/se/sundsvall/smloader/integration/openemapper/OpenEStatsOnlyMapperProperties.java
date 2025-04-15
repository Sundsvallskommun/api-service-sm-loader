package se.sundsvall.smloader.integration.openemapper;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stats-only")
public class OpenEStatsOnlyMapperProperties {
	private Map<String, OpenEMapperProperties> services;

	public Map<String, OpenEMapperProperties> getServices() {
		return services;
	}

	public void setServices(Map<String, OpenEMapperProperties> services) {
		this.services = services;
	}
}
