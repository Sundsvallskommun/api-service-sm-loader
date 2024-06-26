package se.sundsvall.smloader.integration.openemapper.proposal;

import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;
import se.sundsvall.smloader.integration.util.annotation.XPath;

@ExcludeFromJacocoGeneratedCoverageReport
record Proposal(

	@XPath("/Events/FlowInstanceEvent/flowInstanceID")
	String flowInstanceId,

	@XPath("/Events/FlowInstanceEvent/poster/user/firstname")
	String posterFirstName,

	@XPath("/Events/FlowInstanceEvent/poster/user/lastname")
	String posterLastName,

	@XPath("/Events/FlowInstanceEvent/poster/user/username")
	String posterUserName,

	@XPath("/Events/FlowInstanceEvent/poster/user/email")
	String posterEmail) { }
