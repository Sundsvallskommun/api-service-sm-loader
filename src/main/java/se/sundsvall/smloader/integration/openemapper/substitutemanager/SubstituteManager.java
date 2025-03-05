package se.sundsvall.smloader.integration.openemapper.substitutemanager;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record SubstituteManager(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/ordinaryManager/Value") String sentByOrdinaryManager,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/manager/firstname") String managerFirstname,

	@XPath("/FlowInstance/Values/manager/lastname") String managerLastname,

	@XPath("/FlowInstance/Values/manager/username") String managerUserId,

	@XPath("/FlowInstance/Values/manager/citizenIdentifier") String managerLegalId,

	@XPath("/FlowInstance/Values/manager/organization") String managerOrganization,

	@XPath("/FlowInstance/Values/manager2/firstname") String otherSenderFirstname,

	@XPath("/FlowInstance/Values/manager2/lastname") String otherSenderLastname,

	@XPath("/FlowInstance/Values/manager2/username") String otherSenderUserId,

	@XPath("/FlowInstance/Values/manager2/citizenIdentifier") String otherSenderLegalId,

	@XPath("/FlowInstance/Values/manager2/organization") String otherSenderOrganization,

	@XPath("/FlowInstance/Values/responsibilityNumber/Value") String responsibilityNumber,

	@XPath("/FlowInstance/Values/substituteManager/firstname") String substituteManagerFirstname,

	@XPath("/FlowInstance/Values/substituteManager/lastname") String substituteManagerLastname,

	@XPath("/FlowInstance/Values/substituteManager/username") String substituteManagerUserId,

	@XPath("/FlowInstance/Values/substituteManager/citizenIdentifier") String substituteManagerLegalId,

	@XPath("/FlowInstance/Values/substituteManager/organization") String substituteManagerOrganization,

	@XPath("/FlowInstance/Values/date/StartDate") String startDate,

	@XPath("/FlowInstance/Values/date/EndDate") String endDate,

	@XPath("/FlowInstance/Values/approvingManager/firstname") String approvingManagerFirstname,

	@XPath("/FlowInstance/Values/approvingManager/lastname") String approvingManagerLastname,

	@XPath("/FlowInstance/Values/approvingManager/username") String approvingManagerUserId,

	@XPath("/FlowInstance/Values/approvingManager/citizenIdentifier") String approvingManagerLegalId,

	@XPath("/FlowInstance/Values/approvingManager/organization") String approvingManagerOrganization) {}
