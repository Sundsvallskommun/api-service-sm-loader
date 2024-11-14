package se.sundsvall.smloader.integration.util;

public class XPathException extends RuntimeException {

	private static final long serialVersionUID = -5864536630646664680L;

	public XPathException(final String message) {
		super(message);
	}

	public XPathException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
