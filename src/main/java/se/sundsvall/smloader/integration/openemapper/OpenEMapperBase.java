package se.sundsvall.smloader.integration.openemapper;

import generated.se.sundsvall.supportmanagement.ContactChannel;
import generated.se.sundsvall.supportmanagement.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.sundsvall.smloader.service.mapper.OpenEMapper;

import static java.util.Objects.nonNull;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_EMAIL;
import static se.sundsvall.smloader.integration.util.ErrandConstants.CONTACT_CHANNEL_TYPE_PHONE;

public abstract class OpenEMapperBase implements OpenEMapper {

	protected List<ContactChannel> getContactChannels(final String email, final String phone) {

		final var contactChannels = new ArrayList<ContactChannel>();

		if (nonNull(email)) {
			contactChannels.add(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_EMAIL)
				.value(email));
		}

		if (nonNull(phone)) {
			contactChannels.add(new ContactChannel()
				.type(CONTACT_CHANNEL_TYPE_PHONE)
				.value(phone));
		}
		return contactChannels.isEmpty() ? null : contactChannels;
	}

	protected Parameter singleParameter(String value, String key, String displayName) {
		return Optional.ofNullable(value).map(v -> new Parameter(key).values(List.of(v)).displayName(displayName)).orElse(null);
	}

	protected <T> Parameter multiParameter(List<T> list, Function<T, String> join, String key, String displayName) {
		return Optional.ofNullable(list).map(l -> new Parameter(key)
			.values(l.stream().map(join).toList())
			.displayName(displayName))
			.orElse(null);
	}
}
