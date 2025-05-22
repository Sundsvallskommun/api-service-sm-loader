package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import java.util.List;
import se.sundsvall.smloader.integration.util.annotation.GenericType;
import se.sundsvall.smloader.integration.util.annotation.XPath;

record OrderingRecruitmentSupport(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,
	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,
	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,
	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,
	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,
	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,
	@XPath("/FlowInstance/Values/applicant/phone") String applicantPhone,
	@XPath("/FlowInstance/Values/applicant/title") String applicantTitle,
	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/recrutingManager/firstname") String recruitingManagerFirstname,
	@XPath("/FlowInstance/Values/recrutingManager/lastname") String recruitingManagerLastname,
	@XPath("/FlowInstance/Values/recrutingManager/email") String recruitingManagerEmail,
	@XPath("/FlowInstance/Values/recrutingManager/phone") String recruitingManagerPhone,
	@XPath("/FlowInstance/Values/recrutingManager/title") String recruitingManagerTitle,
	@XPath("/FlowInstance/Values/municipalityOrCompany/Value") String municipalityOrCompany,
	@XPath("/FlowInstance/Values/department/Value") String department,
	@XPath("/FlowInstance/Values/referenceNumber/Value") String referenceNumber,
	@XPath("/FlowInstance/Values/securityClassification/Value") String securityClassification,
	@XPath("/FlowInstance/Values/orderDetails/Value") String orderDetails,
	@XPath("/FlowInstance/Values/position/Value") String position,
	@XPath("/FlowInstance/Values/readyProfile/Value") String readyProfile,
	@XPath("/FlowInstance/Values/titleWorkplace/title") String workplaceTitle,
	@XPath("/FlowInstance/Values/titleWorkplace/workplace") String workplace,
	@XPath("/FlowInstance/Values/employmentType/Value") String employmentType,
	@XPath("/FlowInstance/Values/startingDate/StartDate") String startingDate,
	@XPath("/FlowInstance/Values/temporaryDate/StartDate") String temporaryStartDate,
	@XPath("/FlowInstance/Values/temporaryDate/StartEnd") String temporaryEndDate,
	@XPath("/FlowInstance/Values/scope/percentage") String scopePercentage,
	@XPath("/FlowInstance/Values/numberOfPositions/Value") String numberOfPositions,
	@XPath("/FlowInstance/Values/recruitmentAccess/*") @GenericType(RecruitmentAccess.class) List<RecruitmentAccess> recruitmentAccess,
	@XPath("/FlowInstance/Values/workplaceInfo/Value") String workplaceInfo,
	@XPath("/FlowInstance/Values/jobTasks/Value") String jobTasks,
	@XPath("/FlowInstance/Values/qualifications/Value") String qualifications,
	@XPath("/FlowInstance/Values/advertising/Value") String advertising,
	@XPath("/FlowInstance/Values/advertisementContacts/*") @GenericType(AdvertisementContact.class) List<AdvertisementContact> advertisementContacts,
	@XPath("/FlowInstance/Values/unionContacts/*") @GenericType(UnionContact.class) List<UnionContact> unionContacts,
	@XPath("/FlowInstance/Values/addSelectionQuestions/Value") String addSelectionQuestions,
	@XPath("/FlowInstance/Values/question1/Value") String question1,
	@XPath("/FlowInstance/Values/question2/Value") String question2,
	@XPath("/FlowInstance/Values/question3/Value") String question3,
	@XPath("/FlowInstance/Values/advertisementType/Value") String advertisementType,
	@XPath("/FlowInstance/Values/advertisementTime/Value") String advertisementTime,
	@XPath("/FlowInstance/Values/additionalSupport/*") @GenericType(AdditionalSupport.class) List<AdditionalSupport> additionalSupport,
	@XPath("/FlowInstance/Values/mediaChoices/Value") String mediaChoices,
	@XPath("/FlowInstance/Values/advertisementPackage/Value") String advertisementPackage,
	@XPath("/FlowInstance/Values/testType/Value") String testType,
	@XPath("/FlowInstance/Values/inDepthInterview/Value") String inDepthInterview,
	@XPath("/FlowInstance/Values/recruitmentSupport/Value") String recruitmentSupport,
	@XPath("/FlowInstance/Values/additionalInformation/Value") String additionalInformation) {
}
