package se.sundsvall.smloader.integration.util.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.smloader.integration.util.XPathException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class XPathAnnotationProcessorTest {

	private byte[] xml;

	@BeforeEach
	void setUp(@Load("/open-e/misc.xml") final String xml) {
		this.xml = xml.getBytes(UTF_8);
	}

	@Test
	void extractValueThrowsExceptionForAbstractClasses() {
		abstract class DummyClass {

		}

		assertThatExceptionOfType(XPathException.class)
			.isThrownBy(() -> XPathAnnotationProcessor.extractValue(xml, DummyClass.class))
			.withMessageEndingWith("must be a concrete class or a record");
	}

	@Test
	void extractValueThrowsExceptionForInterfaces() {
		interface DummyInterface {

		}

		assertThatExceptionOfType(XPathException.class)
			.isThrownBy(() -> XPathAnnotationProcessor.extractValue(xml, DummyInterface.class))
			.withMessageEndingWith("must be a concrete class or a record");
	}

	@Test
	void extractValueForClass() {
		var result = XPathAnnotationProcessor.extractValue(xml, DishAsClass.class);

		assertThat(result).isNotNull().satisfies(dish -> assertThat(dish.name).isEqualTo("Waffles"));
	}

	@Test
	void extractValueForRecord() {
		var result = XPathAnnotationProcessor.extractValue(xml, DishAsRecord.class);

		assertThat(result).isNotNull().satisfies(dish -> assertThat(dish.name).isEqualTo("Cheeseburger"));
	}

	@Test
	void getValueForString() {
		var dummyStringValue = "someStringValue";

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();

			mockXPathUtil.when(() -> XPathAnnotationProcessor.getString(xml, "/some/path")).thenReturn(dummyStringValue);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", String.class)).isEqualTo(dummyStringValue);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getString(xml, "/some/path"));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getValueForInteger() {
		var dummyIntegerValue = 12345;

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getInteger(xml, "/some/path")).thenReturn(dummyIntegerValue);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", Integer.class)).isEqualTo(dummyIntegerValue);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getInteger(xml, "/some/path"));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getValueForBoolean() {
		var dummyBooleanValue = true;

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getBoolean(xml, "/some/path")).thenReturn(dummyBooleanValue);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", Boolean.class)).isEqualTo(dummyBooleanValue);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getBoolean(xml, "/some/path"));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getValueForDouble() {
		var dummyDoubleValue = 123.45;

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getDouble(xml, "/some/path")).thenReturn(dummyDoubleValue);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", Double.class)).isEqualTo(dummyDoubleValue);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getDouble(xml, "/some/path"));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getValueForFloat() {
		var dummyFloatValue = 123.45f;

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getFloat(xml, "/some/path")).thenReturn(dummyFloatValue);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", Float.class)).isEqualTo(dummyFloatValue);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getFloat(xml, "/some/path"));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getValueForOtherType() {
		var dummyDish = new DishAsRecord("someDummyValue");

		try (var mockXPathUtil = mockStatic(XPathAnnotationProcessor.class)) {
			mockXPathUtil.when(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any())).thenCallRealMethod();
			mockXPathUtil.when(() -> XPathAnnotationProcessor.extractValue(xml, DishAsRecord.class)).thenReturn(dummyDish);

			assertThat(XPathAnnotationProcessor.getValue(xml, "/some/path", DishAsRecord.class)).isEqualTo(dummyDish);

			mockXPathUtil.verify(() -> XPathAnnotationProcessor.getValue(any(), any(String.class), any()));
			mockXPathUtil.verify(() -> XPathAnnotationProcessor.extractValue(xml, DishAsRecord.class));
			mockXPathUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void getString() {
		var first = XPathAnnotationProcessor.getString(xml, "/menu/dish[1]/name");
		var second = XPathAnnotationProcessor.getString(xml, "/menu/dish[2]/name");
		var third = XPathAnnotationProcessor.getString(xml, "/menu/dish[3]/name");

		assertThat(first).isEqualTo("Waffles");
		assertThat(second).isEqualTo("Cheeseburger");
		assertThat(third).isNull();
	}

	@Test
	void getInteger() {
		var first = XPathAnnotationProcessor.getInteger(xml, "/menu/dish[1]/calories");
		var second = XPathAnnotationProcessor.getInteger(xml, "/menu/dish[2]/calories");
		var third = XPathAnnotationProcessor.getInteger(xml, "/menu/dish[3]/calories");

		assertThat(first).isEqualTo(650);
		assertThat(second).isEqualTo(900);
		assertThat(third).isNull();
	}

	@Test
	void getBoolean() {
		var first = XPathAnnotationProcessor.getBoolean(xml, "/menu/dish[1]/vegetarian");
		var second = XPathAnnotationProcessor.getBoolean(xml, "/menu/dish[2]/vegetarian");
		var third = XPathAnnotationProcessor.getBoolean(xml, "/menu/dish[3]/vegetarian");

		assertThat(first).isTrue();
		assertThat(second).isFalse();
		assertThat(third).isNull();
	}

	@Test
	void getDouble() {
		var first = XPathAnnotationProcessor.getDouble(xml, "/menu/dish[1]/price");
		var second = XPathAnnotationProcessor.getDouble(xml, "/menu/dish[2]/price");
		var third = XPathAnnotationProcessor.getDouble(xml, "/menu/dish[3]/price");

		assertThat(first).isEqualTo(55.0);
		assertThat(second).isEqualTo(119.95);
		assertThat(third).isNull();
	}

	@Test
	void getFloat() {
		var first = XPathAnnotationProcessor.getFloat(xml, "/menu/dish[1]/price");
		var second = XPathAnnotationProcessor.getFloat(xml, "/menu/dish[2]/price");
		var third = XPathAnnotationProcessor.getFloat(xml, "/menu/dish[3]/price");

		assertThat(first).isEqualTo(55.0f);
		assertThat(second).isEqualTo(119.95f);
		assertThat(third).isNull();
	}

	static class DishAsClass {

		@XPath("/menu/dish[1]/name")
		private String name;

		private String something;
	}

	record DishAsRecord(@XPath("/menu/dish[2]/name") String name) {}

	@Nested
	class ParameterTests {

		@SuppressWarnings("unused") // Suppressed since it's used only for this test
		private Object dummy;

		@Test
		void constructorAndAccessors() throws Exception {
			var field = getClass().getDeclaredField("dummy");
			var type = getClass();
			var value = "someValue";

			var parameter = new XPathAnnotationProcessor.Parameter(field, type, value);

			assertThat(parameter.field()).isEqualTo(field);
			assertThat(parameter.type()).isEqualTo(type);
			assertThat(parameter.value()).isEqualTo(value);
		}
	}
}
