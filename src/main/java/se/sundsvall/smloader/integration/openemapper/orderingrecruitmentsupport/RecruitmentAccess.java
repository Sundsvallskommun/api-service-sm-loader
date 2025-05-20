package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record RecruitmentAccess(
	@XPath("//Column/value") String name,
	@XPath("//Column1/value") String email,
	@XPath("//Column2/value") String role) {
}
