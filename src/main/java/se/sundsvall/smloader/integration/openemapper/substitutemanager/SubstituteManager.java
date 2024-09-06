package se.sundsvall.smloader.integration.openemapper.substitutemanager;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record SubstituteManager(

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

	@XPath("/FlowInstance/Values/ordinaryManager/Value")
	String sentByOrdinaryManager,

	@XPath("/FlowInstance/Values/manager/firstname")
	String managerFirstname,

	@XPath("/FlowInstance/Values/manager/lastname")
	String managerLastname,

	@XPath("/FlowInstance/Values/manager/username")
	String managerUsername,

	@XPath("/FlowInstance/Values/manager/citizenIdentifier")
	String managerLegalId,

	@XPath("/FlowInstance/Values/manager/organization")
	String managerOrganization,

	@XPath("/FlowInstance/Values/responsibilityNumber/Value")
	String responsibilityNumber,

	@XPath("/FlowInstance/Values/substituteManager/firstname")
	String substituteManagerFirstname,

	@XPath("/FlowInstance/Values/substituteManager/lastname")
	String substituteManagerLastname,

	@XPath("/FlowInstance/Values/substituteManager/username")
	String substituteManagerUsername,

	@XPath("/FlowInstance/Values/substituteManager/citizenIdentifier")
	String substituteManagerLegalId,

	@XPath("/FlowInstance/Values/substituteManager/organization")
	String substituteManagerOrganization,

	@XPath("/FlowInstance/Values/date/StartDate")
	String startDate,

	@XPath("/FlowInstance/Values/date/EndDate")
	String endDate,

	@XPath("/FlowInstance/Values/approvingManager/firstname")
	String approvingManagerFirstname,

	@XPath("/FlowInstance/Values/approvingManager/lastname")
	String approvingManagerLastname,

	@XPath("/FlowInstance/Values/approvingManager/username")
	String approvingManagerUsername,

	@XPath("/FlowInstance/Values/approvingManager/citizenIdentifier")
	String approvingManagerLegalId,

	@XPath("/FlowInstance/Values/approvingManager/organization")
	String approvingManagerOrganization) { }
