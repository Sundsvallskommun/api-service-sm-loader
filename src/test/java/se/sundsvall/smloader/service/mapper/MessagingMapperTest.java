package se.sundsvall.smloader.service.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.messaging.configuration.MessagingProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagingMapperTest {
	@Mock
	private MessagingProperties mockMessagingProperties;

	@InjectMocks
	private MessagingMapper messagingMapper;

	@Test
	void toRequest() {
		// Arrange
		final var message = "message";
		final var token = "token";
		final var channel = "channel";
		when(mockMessagingProperties.token()).thenReturn(token);
		when(mockMessagingProperties.channel()).thenReturn(channel);

		// Act
		final var result = messagingMapper.toRequest(message);

		// Assert
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getToken()).isEqualTo(token);
		assertThat(result.getChannel()).isEqualTo(channel);
	}

	@Test
	void toEmailRequest() {
		// Arrange
		final var subject = "subject";
		final var message = "message";
		final var mailRecipient = "mailRecipient";
		when(mockMessagingProperties.mailRecipient()).thenReturn(mailRecipient);

		// Act
		final var result = messagingMapper.toEmailRequest(subject, message);

		// Assert
		assertThat(result.getSender().getName()).isEqualTo("SmLoader");
		assertThat(result.getSender().getAddress()).isEqualTo("noreply@sundsvall.se");
		assertThat(result.getEmailAddress()).isEqualTo(mailRecipient);
		assertThat(result.getSubject()).isEqualTo(subject);
		assertThat(result.getMessage()).isEqualTo(message);
	}
}
