package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import se.sundsvall.smloader.integration.util.annotation.XPath;

record AdvertisementContact(
	@XPath("//Column/value") String name,
	@XPath("//Column1/value") String title,
	@XPath("//Column2/value") String phoneNumber) {
}
