package se.sundsvall.smloader.integration.openemapper.providefeedback;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record ProvideFeedback(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name") String status,

	@XPath("/FlowInstance/Values/contact/Firstname") String firstname,

	@XPath("/FlowInstance/Values/contact/Lastname") String lastname,

	@XPath("/FlowInstance/Values/contact/Email") String email,

	@XPath("/FlowInstance/Header/Poster/Firstname") String posterFirstname,

	@XPath("/FlowInstance/Header/Poster/Lastname") String posterLastname,

	@XPath("/FlowInstance/Header/Poster/Email") String posterEmail,

	@XPath("/FlowInstance/Values/contact/MobilePhone") String mobilePhone,

	@XPath("/FlowInstance/Values/reportType/Value") String reportType,

	@XPath("/FlowInstance/Values/title/Rubrik") String title,

	@XPath("/FlowInstance/document/QueryID") String documentQueryId,

	@XPath("/FlowInstance/document/File/ID") String documentFileId,

	@XPath("/FlowInstance/Values/description/Value") String description) {}
