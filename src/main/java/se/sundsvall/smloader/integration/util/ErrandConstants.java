package se.sundsvall.smloader.integration.util;

import java.util.Map;

public final class ErrandConstants {

	private ErrandConstants() {
	}

	//TODO Move to properties or db?
	private static final String NAME_SPACE_CONTACTCENTER = "CONTACTCENTER";
	public static final Map<String, String> NAMESPACE_BY_FAMILY_ID = Map.of("161", NAME_SPACE_CONTACTCENTER, "77", NAME_SPACE_CONTACTCENTER);
	public static final String MUNICIPALITY_ID = "2281";
	public static final String ROLE_CONTACT_PERSON = "CONTACT_PERSON";
	public static final String CONTACT_CHANNEL_TYPE_EMAIL = "Email";
	public static final String CONTACT_CHANNEL_TYPE_PHONE = "Phone";
	public static final String CATEGORY_LAMNA_SYNPUNKT = "LAMNA_SYNPUNKT";
	public static final String TYPE_LAMNA_SYNPUNKT = "OTHER";
}
