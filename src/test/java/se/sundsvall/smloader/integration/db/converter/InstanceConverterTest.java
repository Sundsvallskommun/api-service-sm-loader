package se.sundsvall.smloader.integration.db.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InstanceConverterTest {

	private final InstanceConverter instanceConverter = new InstanceConverter();

	@ParameterizedTest
	@EnumSource(value = Instance.class)
	void testConvertToDatabaseColumn(Instance instance) {
		final var value = instanceConverter.convertToDatabaseColumn(instance);
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToDatabaseColumnWhenNullValue() {
		final var value = instanceConverter.convertToDatabaseColumn(null);
		assertThat(value).isNull();
	}

	@ParameterizedTest
	@EnumSource(value = Instance.class)
	void testConvertToEntityAttribute(Instance instance) {
		final var value = instanceConverter.convertToEntityAttribute(instance.name());
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> instanceConverter.convertToEntityAttribute("noMatch"))
			.withMessage("No enum constant se.sundsvall.smloader.integration.db.model.enums.Instance.noMatch");
	}
}
