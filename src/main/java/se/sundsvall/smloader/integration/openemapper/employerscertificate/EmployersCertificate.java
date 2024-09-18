package se.sundsvall.smloader.integration.openemapper.employerscertificate;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record EmployersCertificate(

	@XPath("/FlowInstance/Header/Flow/FamilyID")
	String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID")
	String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name")
	String status,

	@XPath("/FlowInstance/Header/Poster/Firstname")
	String posterFirstname,

	@XPath("/FlowInstance/Header/Poster/Lastname")
	String posterLastname,

	@XPath("/FlowInstance/Header/Poster/Email")
	String posterEmail,

	@XPath("/FlowInstance/Values/applicant/Firstname")
	String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/Lastname")
	String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/SocialSecurityNumber")
	String applicantLegalId,

	@XPath("/FlowInstance/Values/email/Email")
	String applicantEmail,

	@XPath("/FlowInstance/Values/phone/MobilePhone")
	String applicantPhone,

	@XPath("/FlowInstance/Values/periodDates/StartDate")
	String startDate,

	@XPath("/FlowInstance/Values/periodDates/EndDate")
	String endDate,

	@XPath("/FlowInstance/Values/aKassa/Value")
	String unemploymentFund,

	@XPath("/FlowInstance/Values/sendDigital/Value")
	String sendDigital) { }
