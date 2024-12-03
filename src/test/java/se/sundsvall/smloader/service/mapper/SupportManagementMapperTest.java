package se.sundsvall.smloader.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupportManagementMapperTest {

	@Test
	void testToParameter() {

		// Arrange
		final var parameterName = "paramName";
		final var parameterValue = "paramValue";

		// Act
		final var result = SupportManagementMapper.toParameter(parameterName, parameterValue);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getKey()).isEqualTo(parameterName);
		assertThat(result.getValues()).containsExactly(parameterValue);
	}

	@Test
	void testToParameterNullValue() {

		// Arrange
		final var parameterName = "paramName";
		final String parameterValue = null;

		// Act
		final var result = SupportManagementMapper.toParameter(parameterName, parameterValue);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void testToParameterList() {

		// Arrange
		final var parameterName = "paramName";
		final var parameterValue = "paramValue";

		// Act
		final var result = SupportManagementMapper.toParameterList(parameterName, parameterValue);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getKey()).isEqualTo(parameterName);
		assertThat(result.getFirst().getValues()).containsExactly(parameterValue);
	}

	@Test
	void testToParameterListNullValue() {

		// Arrange
		final var parameterName = "paramName";
		final var parameterValue = (String) null;

		// Act
		final var result = SupportManagementMapper.toParameterList(parameterName, parameterValue);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}
}
