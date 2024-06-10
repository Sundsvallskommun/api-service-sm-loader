package se.sundsvall.smloader.integration.db.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

@Converter(autoApply = true)
public class InstanceConverter implements AttributeConverter<Instance, String> {

	@Override
	public String convertToDatabaseColumn(Instance attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toString();
	}

	@Override
	public Instance convertToEntityAttribute(String dbData) {
		return Instance.valueOf(dbData);
	}
}
