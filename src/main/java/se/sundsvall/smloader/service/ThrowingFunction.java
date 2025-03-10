package se.sundsvall.smloader.service;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
	R map(T t) throws E;

	Logger LOGGER = LoggerFactory.getLogger(ThrowingFunction.class);

	/**
	 * Wraps a function with try/catch (Exception) and returns null if exception occurs.
	 *
	 * @param  throwingFunction Function that throws exception
	 * @param  <T>              the type of the input to the function
	 * @param  <R>              the type of the result of the function
	 * @return                  Same as the Function or null if exception is thrown
	 */
	static <T, R> Function<T, R> exceptionToNull(ThrowingFunction<T, R, Exception> throwingFunction) {
		requireNonNull(throwingFunction);
		return t -> {
			try {
				return throwingFunction.map(t);
			} catch (Exception e) {
				LOGGER.error("Error in function! Returning null", e);
				return null;
			}
		};
	}
}
