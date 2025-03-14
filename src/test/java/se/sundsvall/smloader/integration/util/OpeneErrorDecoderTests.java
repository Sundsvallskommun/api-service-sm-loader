package se.sundsvall.smloader.integration.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.Response;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.exception.ClientProblem;

class OpeneErrorDecoderTests {
	private OpenESoapErrorDecoder errorDecoder;

	@BeforeEach
	void setUp() {
		errorDecoder = new OpenESoapErrorDecoder();
	}

	@Test
	void shouldDecodeEmptyResponse() {
		// given
		Response response = mock(Response.class);
		when(response.body()).thenReturn(null);
		when(response.reason()).thenReturn("Bad Request");

		// when
		Exception decodedError = errorDecoder.decode("someMethodKey", response);

		// then
		assertThat(decodedError).isInstanceOf(ClientProblem.class);
		assertThat(decodedError.getMessage()).isEqualTo("Bad Request: Bad request exception from OpenE Bad Request");
	}

	@Test
	void shouldHandleIOException() throws IOException {
		// given
		Response response = mock(Response.class);
		Response.Body body = mock(Response.Body.class);
		when(response.body()).thenReturn(body);
		when(body.asInputStream()).thenThrow(new IOException("Failed to read response body"));

		// when
		Exception decodedError = errorDecoder.decode("someMethodKey", response);

		// then
		assertThat(decodedError).isInstanceOf(ClientProblem.class);
		assertThat(decodedError.getMessage()).isEqualTo("Bad Request: Bad request exception from OpenE Failed to read response body");
	}

	@Test
	void shouldDecodeSOAPFault() throws Exception {
		// given
		Response response = mock(Response.class);
		InputStream inputStream = new ByteArrayInputStream(
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>Internal Server Error</faultstring></soap:Fault></soap:Body></soap:Envelope>".getBytes());
		Response.Body body = mock(Response.Body.class);
		when(response.body()).thenReturn(body);
		when(body.asInputStream()).thenReturn(inputStream);

		OpenESoapErrorDecoder decoder = new OpenESoapErrorDecoder() {
			@Override
			protected SOAPMessage createSOAPMessage(InputStream inputStream) throws SOAPException, IOException {
				SOAPMessage message = mock(SOAPMessage.class);
				SOAPBody soapBody = mock(SOAPBody.class);
				SOAPFault soapFault = mock(SOAPFault.class);
				when(soapBody.hasFault()).thenReturn(true);
				when(soapBody.getFault()).thenReturn(soapFault);
				when(soapFault.getFaultString()).thenReturn("Internal Server Error");
				when(message.getSOAPBody()).thenReturn(soapBody);
				return message;
			}
		};

		// when
		Exception decodedError = decoder.decode("someMethodKey", response);

		// then
		assertThat(decodedError).isInstanceOf(ClientProblem.class);
		assertThat(decodedError.getMessage()).isEqualTo("Bad Request: Bad request exception from OpenE Internal Server Error");
	}
}
