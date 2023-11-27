package com.siryus.swisscon.api.util;

import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ValidationUtils {
    public static void throwIfNot(boolean condition, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }

    public static void throwIf(boolean condition, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (condition) {
            throw exceptionSupplier.get();
        }
    }

    public static <T> T throwIf(T object, Predicate<T> condition,
                                Function<T, LocalizedResponseStatusException> exceptionCreator) {
        if (condition.test(object)) {
            throw exceptionCreator.apply(object);
        }
        return object;
    }

    public static <T> T throwIfNot(T object, Predicate<T> condition, Function<T, LocalizedResponseStatusException>  exceptionCreator) {
        if (!condition.test(object)) {
            throw exceptionCreator.apply(object);
        }
        return object;
    }

    public static <T> T throwIfNull(T object, Supplier<LocalizedResponseStatusException> exceptionSupplier) {
        if (object == null) {
            throw exceptionSupplier.get();
        }
        return object;
    }

    public static <T> T throwIfNotNull(T object, Supplier<LocalizedResponseStatusException> exceptionSupplier) {
        if (object != null) {
            throw exceptionSupplier.get();
        }
        return object;
    }

    public static <T> List<T> throwIfEmpty(List<T> list, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (list.isEmpty()) {
            throw  exceptionSupplier.get();
        }
        return list;
    }

    public static <T> List<T> throwIfNotEmpty(List<T> list, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (!list.isEmpty()) {
            throw  exceptionSupplier.get();
        }
        return list;
    }

    public static <T> void throwIfOneOf(T target, List<T> expectedValues, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (expectedValues.contains(target)) {
            throw exceptionSupplier.get();
        }
    }
    public static <T> void throwIfNotOneOf(T target, List<T> expectedValues, Supplier<LocalizedResponseStatusException>  exceptionSupplier) {
        if (!expectedValues.contains(target)) {
            throw exceptionSupplier.get();
        }
    }
}
