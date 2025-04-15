package se.sundsvall.smloader.integration.openemapper.statsonly;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record StatsOnly(

	@XPath("/FlowInstance/Header/Flow/FamilyID") String familyId,

	@XPath("/FlowInstance/Header/FlowInstanceID") String flowInstanceId) {}
