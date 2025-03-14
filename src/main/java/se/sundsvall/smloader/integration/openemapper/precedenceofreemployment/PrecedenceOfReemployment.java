package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record PrecedenceOfReemployment(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/employmentInformation/workPlace") String workplace,

	@XPath("/FlowInstance/Values/employmentInformation/position") String position,

	@XPath("/FlowInstance/Values/employmentInformation/manager") String manager,

	@XPath("/FlowInstance/Values/lastDayOfPosition/StartDate") String lastDayOfPosition,

	@XPath("/FlowInstance/Values/salaryType/Value") String salaryType,

	@XPath("/FlowInstance/Values/applicant/Fornamn") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/Efternamn") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/Personnummer") String applicantLegalId,

	@XPath("/FlowInstance/Values/applicant/Adress") String applicantAddress,

	@XPath("/FlowInstance/Values/applicant/Postnummer") String applicantZipCode,

	@XPath("/FlowInstance/Values/applicant/Postadress") String applicantPostalAddress,

	@XPath("/FlowInstance/Values/applicant/E-post__privat_") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/Telefonnummer__privat_") String applicantPhone) {
}
