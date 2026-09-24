package se.sundsvall.smloader.integration.openemapper.facilityorder;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record FacilityOrder(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/phone") String applicantPhone,

	@XPath("/FlowInstance/Values/applicant/mobilePhone") String applicantMobilePhone,

	@XPath("/FlowInstance/Values/applicant/title") String applicantTitle,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/department/Value") String administration,

	@XPath("/FlowInstance/Values/operation/Value") String operation,

	@XPath("/FlowInstance/Values/orderDetails/Value") String orderDetails,

	@XPath("/FlowInstance/Values/workplaceProperty/Value") String property,

	@XPath("/FlowInstance/Values/workplaceWorkplace/Value") String workplaceUnit,

	@XPath("/FlowInstance/Values/workplaceRoom/Value") String room,

	@XPath("/FlowInstance/Values/operationInformation/Value") String operationInformation) {}
