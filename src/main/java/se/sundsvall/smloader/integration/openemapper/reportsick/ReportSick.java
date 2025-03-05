package se.sundsvall.smloader.integration.openemapper.reportsick;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record ReportSick(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/applicant/firstname") String applicantFirstname,

	@XPath("/FlowInstance/Values/applicant/lastname") String applicantLastname,

	@XPath("/FlowInstance/Values/applicant/username") String applicantUserId,

	@XPath("/FlowInstance/Values/applicant/email") String applicantEmail,

	@XPath("/FlowInstance/Values/applicant/phone") String applicantPhone,

	@XPath("/FlowInstance/Values/applicant/organization") String applicantOrganization,

	@XPath("/FlowInstance/Values/administrativeUnit/Value") String administrativeUnit,

	@XPath("/FlowInstance/Values/employeeData/firstname") String employeeFirstname,

	@XPath("/FlowInstance/Values/employeeData/lastname") String employeeLastname,

	@XPath("/FlowInstance/Values/employeeData/citizenidentifier") String employeeLegalId,

	@XPath("/FlowInstance/Values/employeeData/organization") String employeeOrganization,

	@XPath("/FlowInstance/Values/employmentType/Value") String employmentType,

	@XPath("/FlowInstance/Values/employeeTitle/Value") String employeeTitle,

	@XPath("/FlowInstance/Values/levelOfAbsence/Value") String levelOfAbsence,

	@XPath("/FlowInstance/Values/absentDateStart/StartDate") String absentStartDate,

	@XPath("/FlowInstance/Values/absentDateSpecific/Value") String absentDescription,

	@XPath("/FlowInstance/Values/absentDateLeaveTime/StartTime") String absentStartTime,

	@XPath("/FlowInstance/Values/absentContunuation/Value") String absentContinuation,

	@XPath("/FlowInstance/Values/absentDateLateStart/StartTime") String absentLateStartTime,

	@XPath("/FlowInstance/Values/absentDatePeriod/StartDate") String absentPeriodStartDate,

	@XPath("/FlowInstance/Values/absentDatePeriod/EndDate") String absentPeriodEndDate,

	@XPath("/FlowInstance/Values/haveSickNote/Value") String haveSickNote,

	@XPath("/FlowInstance/Values/absentNewOld/Value") String absentType,

	@XPath("/FlowInstance/Values/absentDateFrom/StartDate") String absentFirstDay,

	@XPath("/FlowInstance/Values/sickNotePeriod/Value") Integer countOfSickLeavePeriods) {}
