package se.sundsvall.smloader.integration.openemapper.contactsalaryandpension;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record ContactSalaryAndPension(

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

	@XPath("/FlowInstance/Values/contact/firstname")
	String contactFirstname,

	@XPath("/FlowInstance/Values/contact/lastname")
	String contactLastname,

	@XPath("/FlowInstance/Values/contact/citizenIdentifier")
	String contactLegalId,

	@XPath("/FlowInstance/Values/contact/organization")
	String contactOrganization,

	@XPath("/FlowInstance/Values/subject/TextAlternative")
	String subject,

	@XPath("/FlowInstance/Values/caseDescription/Value")
	String description) { }
