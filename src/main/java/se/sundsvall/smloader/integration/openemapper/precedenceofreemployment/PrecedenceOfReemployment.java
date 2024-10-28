package se.sundsvall.smloader.integration.openemapper.precedenceofreemployment;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record PrecedenceOfReemployment(

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

	@XPath("/FlowInstance/Values/workPlace/Ange_arbetsplats")
	String workplace,

	@XPath("/FlowInstance/Values/position/Value")
	String position,

	@XPath("/FlowInstance/Values/lastDayOfPosition/StartDate")
	String lastDayOfPosition,

	@XPath("/FlowInstance/Values/salaryType/Value")
	String salaryType,

	@XPath("/FlowInstance/Values/manager/firstname")
	String managerFirstname,

	@XPath("/FlowInstance/Values/manager/lastname")
	String managerLastname,

	@XPath("/FlowInstance/Values/manager/email")
	String managerEmail,

	@XPath("/FlowInstance/Values/manager/organization")
	String managerOrganization,

	@XPath("/FlowInstance/Values/applicant/Fornamn")
	String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/Efternamn")
	String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/E-post__privat_")
	String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/Telefonnummer__privat_")
	String applicantPhone) { }
