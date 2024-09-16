package se.sundsvall.smloader.integration.openemapper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenEMapperConfiguration {

	@Bean(name = "proposal")
	@ConfigurationProperties(prefix = "sundsvallsforslaget")
	OpenEMapperProperties propertiesProposal() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "feedback")
	@ConfigurationProperties(prefix = "lamna-synpunkt")
	OpenEMapperProperties propertiesFeedback() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "permissionorder")
	@ConfigurationProperties(prefix = "ny-behorighet")
	OpenEMapperProperties propertiesPermissionOrder() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "substitutemanager")
	@ConfigurationProperties(prefix = "ersattare-chef")
	OpenEMapperProperties propertiesSubstituteManager() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "salarychange")
	@ConfigurationProperties(prefix = "lonevaxling-pension")
	OpenEMapperProperties propertiesSalaryChange() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "contactsalaryandpension")
	@ConfigurationProperties(prefix = "kontakt-lon-pension")
	OpenEMapperProperties propertiesContactSalaryAndPension() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "twentyfiveatwork")
	@ConfigurationProperties(prefix = "tjugofem-ar-pa-jobbet")
	OpenEMapperProperties propertiesTwentyFiveAtWork() {
		return new OpenEMapperProperties();
	}
}
