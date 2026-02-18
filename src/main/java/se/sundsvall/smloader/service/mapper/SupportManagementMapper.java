package se.sundsvall.smloader.service.mapper;

import generated.se.sundsvall.supportmanagement.Parameter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public final class SupportManagementMapper {

	private SupportManagementMapper() {}

	public static List<Parameter> toParameterList(String parameterName, String value) {
		final var parameterList = new ArrayList<Parameter>();
		ofNullable(toParameter(parameterName, value, null)).ifPresent(parameterList::add);
		return parameterList;
	}

	public static Parameter toParameter(String parameterName, String value, String displayName) {
		return ofNullable(value)
			.map(v -> new Parameter().key(parameterName).values(asList(v)).displayName(displayName))
			.orElse(null);
	}
}
