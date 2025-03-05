package se.sundsvall.smloader.integration.openemapper.salarychange;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record SalaryChange(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Header/Owner/Firstname") String ownerFirstname,

	@XPath("/FlowInstance/Header/Owner/Lastname") String ownerLastname,

	@XPath("/FlowInstance/Header/Owner/Email") String ownerEmail,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/citizenIdentifier") String applicantLegalId,

	@XPath("/FlowInstance/Values/applicant/title") String applicantTitle,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/salary/Ange_det_belopp_du_vill_lonevaxla__minimum_500kr_") String amount,

	@XPath("/FlowInstance/Values/fromMonth/Value") String fromMonth) {}
