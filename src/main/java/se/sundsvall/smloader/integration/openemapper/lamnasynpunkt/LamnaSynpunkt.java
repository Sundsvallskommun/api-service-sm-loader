package se.sundsvall.smloader.integration.openemapper.lamnasynpunkt;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record LamnaSynpunkt(

	@XPath("/FlowInstance/Header/Flow/FamilyID")
	String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID")
	String flowInstanceId,

	@XPath("/FlowInstance/Header/Status/Name")
	String status,

	@XPath("/FlowInstance/Values/contact/Firstname")
	String fornamn,

	@XPath("/FlowInstance/Values/contact/Lastname")
	String efternamn,

	@XPath("/FlowInstance/Values/contact/Email")
	String epost,

	@XPath("/FlowInstance/Values/contact/MobilePhone")
	String mobilnummer,

	@XPath("/FlowInstance/Values/reportType/Value")
	String reportType,

	@XPath("/FlowInstance/Values/title/Rubrik")
	String rubrik,

	@XPath("/FlowInstance/Values/description/Value")
	String beskrivning) { }
