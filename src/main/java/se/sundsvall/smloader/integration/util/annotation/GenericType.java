package se.sundsvall.smloader.integration.util.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({
	FIELD
})
@Retention(RUNTIME)
public @interface GenericType {
	Class<?> value();
}
