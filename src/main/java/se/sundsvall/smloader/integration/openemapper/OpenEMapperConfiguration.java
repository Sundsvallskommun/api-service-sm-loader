package se.sundsvall.smloader.integration.openemapper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenEMapperConfiguration {

	@Bean(name = "proposal")
	@ConfigurationProperties(prefix = "sundsvallsforslaget")
	public OpenEMapperProperties propertiesProposal() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "feedback")
	@ConfigurationProperties(prefix = "lamna-synpunkt")
	public OpenEMapperProperties propertiesFeedback() {
		return new OpenEMapperProperties();
	}
}
