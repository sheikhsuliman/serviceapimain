package com.siryus.swisscon.api.base;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TestResultLoggerExtension implements BeforeEachCallback, AfterEachCallback {
    private static final int LONG_TEST_SECONDS = 2;

    private static final Logger LOG = LoggerFactory.getLogger(TestResultLoggerExtension.class);

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        LocalDateTime startTime = (LocalDateTime) context.getStore(ExtensionContext.Namespace.GLOBAL).get(a("startTime"));

        long testRunTimeSec = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        if( testRunTimeSec > LONG_TEST_SECONDS) {
            LOG.warn("Test {} runs too long {} sec", context.getDisplayName(),testRunTimeSec);
        }
        else {
            LOG.debug("Test {} finished in {} msec: ", context.getDisplayName(), ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()));
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(a("startTime"), LocalDateTime.now());
        LOG.debug("Test started: {}: ", context.getDisplayName());
    }

    private static String a(String name) {
        return TestResultLoggerExtension.class.getSimpleName() + ":" + name;
    }
}