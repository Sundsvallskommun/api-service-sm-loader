package se.sundsvall.smloader.integration.openemapper.proposal;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record Proposal(

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

	@XPath("/FlowInstance/Values/contact/Firstname")
	String firstname,

	@XPath("/FlowInstance/Values/contact/Lastname")
	String lastname,

	@XPath("/FlowInstance/Values/contact/Address")
	String address,

	@XPath("/FlowInstance/Values/contact/ZipCode")
	String zipCode,

	@XPath("/FlowInstance/Values/contact/PostalAddress")
	String postalAddress,

	@XPath("/FlowInstance/Values/contact/ContactBySMS")
	Boolean contactBySMS,

	@XPath("/FlowInstance/Values/contact/Email")
	String email,

	@XPath("/FlowInstance/Values/contact/MobilePhone")
	String mobilePhone,

	@XPath("/FlowInstance/Values/title/Value")
	String title,

	@XPath("/FlowInstance/document/QueryID")
	String documentQueryId,

	@XPath("/FlowInstance/document/File/ID")
	String documentFileId,

	@XPath("/FlowInstance/Values/description/Value")
	String description) { }
