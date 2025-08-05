package se.sundsvall.smloader.integration.util;

import static org.jsoup.Jsoup.parse;
import static org.jsoup.parser.Parser.xmlParser;

import java.nio.charset.StandardCharsets;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.xsoup.Xsoup;

public final class XPathUtil {

	private XPathUtil() {}

	public static Document parseXmlDocument(final byte[] xml) {
		return parse(new String(xml, StandardCharsets.UTF_8), xmlParser());
	}

	public static Elements evaluateXPath(final byte[] xml, final String expression) {
		final var doc = parseXmlDocument(xml);

		return Xsoup.compile(expression).evaluate(doc).getElements();
	}

	public static Elements evaluateXPath(final Element element, final String expression) {
		return Xsoup.compile(expression).evaluate(element).getElements();
	}
}
