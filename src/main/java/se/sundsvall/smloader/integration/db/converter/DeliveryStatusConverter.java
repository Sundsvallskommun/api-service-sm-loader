package se.sundsvall.smloader.integration.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.sundsvall.smloader.integration.db.model.enums.DeliveryStatus;

@Converter(autoApply = true)
public class DeliveryStatusConverter implements AttributeConverter<DeliveryStatus, String> {

	@Override
	public String convertToDatabaseColumn(DeliveryStatus attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toString();
	}

	@Override
	public DeliveryStatus convertToEntityAttribute(String dbData) {
		return DeliveryStatus.valueOf(dbData);
	}
}
