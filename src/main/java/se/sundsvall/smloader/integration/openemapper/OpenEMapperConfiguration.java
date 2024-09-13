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

	@Bean(name = "permissionorder")
	@ConfigurationProperties(prefix = "ny-behorighet")
	public OpenEMapperProperties propertiesPermissionOrder() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "substitutemanager")
	@ConfigurationProperties(prefix = "ersattare-chef")
	public OpenEMapperProperties propertiesSubstituteManager() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "salarychange")
	@ConfigurationProperties(prefix = "lonevaxling-pension")
	public OpenEMapperProperties propertiesSalaryChange() {
		return new OpenEMapperProperties();
	}

	@Bean(name = "contactsalaryandpension")
	@ConfigurationProperties(prefix = "kontakt-lon-pension")
	public OpenEMapperProperties propertiesContactSalaryAndPension() {
		return new OpenEMapperProperties();
	}
}
