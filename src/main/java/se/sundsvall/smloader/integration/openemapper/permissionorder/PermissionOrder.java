package se.sundsvall.smloader.integration.openemapper.permissionorder;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record PermissionOrder(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Header/Poster/Firstname") String posterFirstname,

	@XPath("/FlowInstance/Header/Poster/Lastname") String posterLastname,

	@XPath("/FlowInstance/Header/Poster/Email") String posterEmail,

	@XPath("/FlowInstance/Values/isManager/Value") String isManager,

	@XPath("/FlowInstance/Values/notEmploye/Value") String notEmployee,

	@XPath("/FlowInstance/Values/manager/firstname") String managerFirstname,

	@XPath("/FlowInstance/Values/manager/lastname") String managerLastname,

	@XPath("/FlowInstance/Values/manager/username") String managerUserId,

	@XPath("/FlowInstance/Values/manager/email") String managerEmail,

	@XPath("/FlowInstance/Values/manager/title") String managerTitle,

	@XPath("/FlowInstance/Values/manager/organization") String managerOrganization,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/title") String applicantTitle,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/user/firstname") String userFirstname,

	@XPath("/FlowInstance/Values/user/lastname") String userLastname,

	@XPath("/FlowInstance/Values/user/username") String userUsername,

	@XPath("/FlowInstance/Values/user/citizenIdentifier") String userLegalId,

	@XPath("/FlowInstance/Values/user/email") String userEmail,

	@XPath("/FlowInstance/Values/user/title") String userTitle,

	@XPath("/FlowInstance/Values/user/organization") String userOrganization,

	@XPath("/FlowInstance/Values/computerId/Ange_datornamnet_som_borjar_med_WB") String computerId,

	@XPath("/FlowInstance/Values/administrativeUnit/Value") String administrativeUnit,

	@XPath("/FlowInstance/Values/userTypeHeroma/Value") String userTypeHeroma,

	@XPath("/FlowInstance/Values/accessTypeHeroma/Value") String accessTypeHeroma,

	@XPath("/FlowInstance/Values/uppdateDescription/Value") String updateDescription,

	@XPath("/FlowInstance/Values/typeOfAccess/Value") String typeOfAccess,

	@XPath("/FlowInstance/Values/stillEmployed/Value") String stillEmployed,

	@XPath("/FlowInstance/Values/nameTerminated/Value") String nameTerminated,

	@XPath("/FlowInstance/Values/systemAccess/Value") String systemAccess,

	@XPath("/FlowInstance/Values/fromDate/StartDate") String startDate,

	@XPath("/FlowInstance/Values/accessTemplate/firstname") String templateFirstname,

	@XPath("/FlowInstance/Values/accessTemplate/lastname") String templateLastname,

	@XPath("/FlowInstance/Values/accessTemplate/username") String templateUsername,

	@XPath("/FlowInstance/Values/accessTemplate/citizenIdentifier") String templateLegalId,

	@XPath("/FlowInstance/Values/accessTemplate/title") String templateTitle,

	@XPath("/FlowInstance/Values/accessTemplate/organization") String templateOrganization,

	@XPath("/FlowInstance/Values/otherInformation/Value") String otherInformation) {}
