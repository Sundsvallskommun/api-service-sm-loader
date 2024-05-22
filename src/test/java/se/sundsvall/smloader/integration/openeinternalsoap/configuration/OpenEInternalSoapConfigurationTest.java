package se.sundsvall.smloader.integration.openeinternalsoap.configuration;

import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.smloader.integration.util.OpenESoapErrorDecoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenEInternalSoapConfigurationTest {

	@Mock
	private ClientRegistrationRepository clientRegistrationRepositoryMock;

	@Mock
	private ClientRegistration clientRegistrationMock;

	@Spy
	private FeignMultiCustomizer feignMultiCustomizerSpy;

	@Mock
	private FeignBuilderCustomizer feignBuilderCustomizerMock;

	@Mock
	private OpenEInternalSoapProperties propertiesMock;

	@Test
	void testFeignBuilderCustomizer() {
		final var configuration = new OpenEInternalSoapConfiguration();

		when(propertiesMock.connectTimeout()).thenReturn(1);
		when(propertiesMock.readTimeout()).thenReturn(2);
		when(propertiesMock.username()).thenReturn("username");
		when(propertiesMock.password()).thenReturn("password");
		when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);

		try (final MockedStatic<FeignMultiCustomizer> feignMultiCustomizerMock = Mockito.mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			final var customizer = configuration.feignBuilderCustomizer(propertiesMock);

			final ArgumentCaptor<OpenESoapErrorDecoder> errorDecoderCaptor = ArgumentCaptor.forClass(OpenESoapErrorDecoder.class);
			final ArgumentCaptor<SOAPEncoder> soapEncoderCaptor = ArgumentCaptor.forClass(SOAPEncoder.class);
			final ArgumentCaptor<SOAPDecoder> soapDecoderCaptor = ArgumentCaptor.forClass(SOAPDecoder.class);

			verify(feignMultiCustomizerSpy).withErrorDecoder(errorDecoderCaptor.capture());
			verify(feignMultiCustomizerSpy).withEncoder(soapEncoderCaptor.capture());
			verify(feignMultiCustomizerSpy).withDecoder(soapDecoderCaptor.capture());
			verify(propertiesMock).connectTimeout();
			verify(propertiesMock).readTimeout();
			verify(propertiesMock).username();
			verify(propertiesMock).password();
			verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(1, 2);
			verify(feignMultiCustomizerSpy).composeCustomizersToOne();

			assertThat(errorDecoderCaptor.getValue()).isNotNull();
			assertThat(soapEncoderCaptor.getValue()).isNotNull();
			assertThat(soapDecoderCaptor.getValue()).isNotNull();
			assertThat(customizer).isSameAs(feignBuilderCustomizerMock);
		}
	}
}
