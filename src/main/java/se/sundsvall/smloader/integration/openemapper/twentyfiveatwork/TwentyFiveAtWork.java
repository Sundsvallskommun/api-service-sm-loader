package se.sundsvall.smloader.integration.openemapper.twentyfiveatwork;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record TwentyFiveAtWork(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/citizenIdentifier") String applicantLegalId,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/personalDetails/firstname") String firstname,

	@XPath("/FlowInstance/Values/personalDetails/lastname") String lastname,

	@XPath("/FlowInstance/Values/personalDetails/email") String email,

	@XPath("/FlowInstance/Values/personalDetails/Address") String address,

	@XPath("/FlowInstance/Values/personalDetails/ZipCode") String zipCode,

	@XPath("/FlowInstance/Values/personalDetails/PostalAddress") String postalAddress,

	@XPath("/FlowInstance/Values/alternativeAdress/Address") String otherAddress,

	@XPath("/FlowInstance/Values/alternativeAdress/ZipCode") String otherZipCode,

	@XPath("/FlowInstance/Values/alternativeAdress/PostalAddress") String otherPostalAddress,

	@XPath("/FlowInstance/Values/personalDetails/SocialSecurityNumber") String legalId,

	@XPath("/FlowInstance/Values/startDate/StartDate") String originalStartDate,

	@XPath("/FlowInstance/Values/StartDateChangeWork/StartDate") String startDateChangeWork,

	@XPath("/FlowInstance/Values/title/Value") String title) {}
