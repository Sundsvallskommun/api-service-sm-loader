package se.sundsvall.smloader.integration.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class XPathUtilTest {

	private byte[] xml;

	@BeforeEach
	void setUp(@Load("/open-e/misc.xml") final String xml) {
		this.xml = xml.getBytes(UTF_8);
	}

	@Test
	void parseXmlDocument() {
		final var document = XPathUtil.parseXmlDocument(xml);
		assertThat(document.getAllElements()).isNotNull();
		assertThat(document.getElementsByTag("menu")).hasSize(1);
		assertThat(document.getElementsByTag("dish")).hasSize(2);
	}

	@Test
	void evaluateXpathFromXml() {
		assertThat(XPathUtil.evaluateXPath(xml, "//menu")).hasSize(1);
	}

	@Test
	void evaluateXpathFromElement() {
		final var elements = XPathUtil.evaluateXPath(xml, "//menu");

		assertThat(XPathUtil.evaluateXPath(elements.first(), "//dish")).hasSize(2);
	}
}
