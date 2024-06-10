package se.sundsvall.smloader.integration.db.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DeliveryStatusConverterTest {

	private final DeliveryStatusConverter deliveryStatusConverter = new DeliveryStatusConverter();

	@ParameterizedTest
	@EnumSource(value = DeliveryStatus.class)
	void testConvertToDatabaseColumn(DeliveryStatus deliveryStatus) {
		final var value = deliveryStatusConverter.convertToDatabaseColumn(deliveryStatus);
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToDatabaseColumnWhenNullValue() {
		final var value = deliveryStatusConverter.convertToDatabaseColumn(null);
		assertThat(value).isNull();
	}

	@ParameterizedTest
	@EnumSource(value = DeliveryStatus.class)
	void testConvertToEntityAttribute(DeliveryStatus deliveryStatus) {
		final var value = deliveryStatusConverter.convertToEntityAttribute(deliveryStatus.name());
		assertThat(value).isNotNull();
	}

	@Test
	void testConvertToEntityAttribute_whenMissingValue_should() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> deliveryStatusConverter.convertToEntityAttribute("noMatch"))
			.withMessage("No enum constant se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus.noMatch");
	}
}
