package se.sundsvall.smloader.integration.openemapper.reportsick;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record ReportSick(

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

	@XPath("/FlowInstance/Values/applicant/firstname")
	String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname")
	String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username")
	String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email")
	String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/phone")
	String applicantPhone,

	@XPath("/FlowInstance/Values/applicant/organization")
	String applicantOrganization,

	@XPath("/FlowInstance/Values/administrativeUnit/Value")
	String administrativeUnit,

	@XPath("/FlowInstance/Values/employeeData/firstname")
	String employeeFirstname,

	@XPath("/FlowInstance/Values/employeeData/lastname")
	String employeeLastname,

	@XPath("/FlowInstance/Values/employeeData/citizenidentifier")
	String employeeLegalId,

	@XPath("/FlowInstance/Values/employeeData/organization")
	String employeeOrganization,

	@XPath("/FlowInstance/Values/employmentType/Value")
	String employmentType,

	@XPath("/FlowInstance/Values/absentDateFrom/StartDate")
	String absentStartDate,

	@XPath("/FlowInstance/Values/sickNotePeriod/Value")
	Integer countOfSickLeavePeriods) { }
