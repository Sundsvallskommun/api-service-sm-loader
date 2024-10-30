package se.sundsvall.smloader.integration.openemapper.contactsalaryandpension;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record ContactSalaryAndPension(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Header/Poster/Firstname") String posterFirstname,

	@XPath("/FlowInstance/Header/Poster/Lastname") String posterLastname,

	@XPath("/FlowInstance/Header/Poster/Email") String posterEmail,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/citizenIdentifier") String applicantLegalId,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/contactManager/firstname") String managerFirstname,

	@XPath("/FlowInstance/Values/contactManager/lastname") String managerLastname,

	@XPath("/FlowInstance/Values/contactManager/username") String managerUserId,

	@XPath("/FlowInstance/Values/contactManager/citizenIdentifier") String managerLegalId,

	@XPath("/FlowInstance/Values/contactManager/organization") String managerOrganization,

	@XPath("/FlowInstance/Values/subject/Value") String subject,

	@XPath("/FlowInstance/Values/caseDescription/Value") String description) {}
