package se.sundsvall.smloader.integration.util.annotation;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

import java.lang.reflect.Field;
import java.util.Optional;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.annotation.AnnotationUtils;
import se.sundsvall.smloader.integration.util.XPathException;

public final class XPathAnnotationProcessor {

	public static <T> T extractValue(final byte[] xml, final Class<T> targetClass) {
		// Check if objects of the given target class can be instantiated
		if (isAbstract(targetClass.getModifiers()) || isInterface(targetClass.getModifiers())) {
			throw new XPathException("%s must be a concrete class or a record".formatted(targetClass.getName()));
		}

		// Get the fields on the target class
		var fields = targetClass.getDeclaredFields();
		// Use arrays to ensure proper parameter ordering for records
		var parameters = new Parameter[fields.length];
		// Process the fields
		for (var i = 0; i < fields.length; i++) {
			var field = fields[i];
			// Get the path annotation for the field
			var pathAnnotation = AnnotationUtils.getAnnotation(field, XPath.class);
			// We can't do anything with the field if the annotation is missing - bail out
			if (pathAnnotation == null) {
				continue;
			}

			var type = field.getType();
			var path = pathAnnotation.value();

			var value = getValue(xml, path, type);

			parameters[i] = new Parameter(field, type, value);
		}

		try {
			if (targetClass.isRecord()) {
				var parameterTypes = new Class[parameters.length];
				var parameterValues = new Object[parameters.length];
				for (var i = 0; i < parameters.length; i++) {
					parameterTypes[i] = parameters[i].type;
					parameterValues[i] = parameters[i].value;
				}

				var constructor = targetClass.getDeclaredConstructor(parameterTypes);
				constructor.setAccessible(true);
				return constructor.newInstance(parameterValues);
			} else {
				var constructor = targetClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				var result = constructor.newInstance();

				for (var parameter : parameters) {
					if (parameter != null) {
						parameter.field.setAccessible(true);
						parameter.field.set(result, parameter.value);
					}
				}

				return result;
			}
		} catch (Exception e) {
			throw new XPathException("Unable to extract value", e);
		}
	}

	public static <T> T getValue(final byte[] xml, final String path, final Class<T> type) {
		Object value;

		value = switch (type.getSimpleName()) {
			case "String" -> getString(xml, path);
			case "Integer" -> getInteger(xml, path);
			case "Boolean" -> getBoolean(xml, path);
			case "Double" -> getDouble(xml, path);
			case "Float" -> getFloat(xml, path);
			default -> extractValue(xml, type);
		};

		return type.cast(value);
	}

	public static String getString(final byte[] xml, final String xPath) {
		return getValue(xml, xPath).orElse(null);
	}

	public static Integer getInteger(final byte[] xml, final String xPath) {
		return getValue(xml, xPath).map(Integer::valueOf).orElse(null);
	}

	public static Boolean getBoolean(final byte[] xml, final String xPath) {
		return getValue(xml, xPath).map(Boolean::valueOf).orElse(null);
	}

	public static Double getDouble(final byte[] xml, final String xPath) {
		return getValue(xml, xPath).map(Double::valueOf).orElse(null);
	}

	public static Float getFloat(final byte[] xml, final String xPath) {
		return getValue(xml, xPath).map(Float::valueOf).orElse(null);
	}

	private static Optional<String> getValue(final byte[] xml, final String path) {
		return ofNullable(evaluateXPath(xml, path))
			.filter(not(Elements::isEmpty))
			.map(Elements::getFirst)
			.map(Element::text);
	}

	record Parameter(Field field, Class<?> type, Object value) {
	}
}
