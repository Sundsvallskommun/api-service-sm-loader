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
	String startDate,

	@XPath("/FlowInstance/Values/manager/firstname")
	String managerFirstname,

	@XPath("/FlowInstance/Values/manager/lastname")
	String managerLastname,

	@XPath("/FlowInstance/Values/manager/email")
	String managerEmail,

	@XPath("/FlowInstance/Values/manager/organization")
	String managerOrganization,

	@XPath("/FlowInstance/Values/contactInformation/E-post__privat_")
	String privateEmail,

	@XPath("/FlowInstance/Values/contactInformation/Telefonnummer__privat_")
	String privatePhone) { }
