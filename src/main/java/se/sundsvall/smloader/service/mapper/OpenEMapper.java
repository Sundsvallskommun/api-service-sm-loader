package se.sundsvall.smloader.service.mapper;

import generated.se.sundsvall.supportmanagement.Errand;

public interface OpenEMapper {
	String getSupportedFamilyId();

	Errand mapToErrand(byte[] xml);
}
