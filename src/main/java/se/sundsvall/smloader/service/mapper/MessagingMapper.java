package se.sundsvall.smloader.service.mapper;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import generated.se.sundsvall.messaging.SlackRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.smloader.integration.messaging.configuration.MessagingProperties;

@Component
public class MessagingMapper {

	private final MessagingProperties properties;

	public MessagingMapper(final MessagingProperties properties) {
		this.properties = properties;
	}

	public SlackRequest toRequest(String message) {
		return new SlackRequest()
			.message(message)
			.token(properties.token())
			.channel(properties.channel());
	}

	public EmailRequest toEmailRequest(String subject, String message) {
		return new EmailRequest()
			.sender(new EmailSender()
				.name("SmLoader")
				.address("noreply@sundsvall.se"))
			.emailAddress(properties.mailRecipient())
			.subject(subject)
			.message(message);
	}
}
