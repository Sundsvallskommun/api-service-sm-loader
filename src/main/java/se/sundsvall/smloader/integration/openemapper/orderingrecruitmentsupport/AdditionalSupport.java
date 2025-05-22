package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record AdditionalSupport(
	@XPath("/Value") String value) {
}
