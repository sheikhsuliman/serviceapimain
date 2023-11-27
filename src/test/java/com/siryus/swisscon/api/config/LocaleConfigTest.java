package com.siryus.swisscon.api.config;

import ch.qos.logback.classic.LoggerContext;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to see if the UTC timezone are correct serverside
 * It's necessary to extend from {@link AbstractMvcTestBase} because we
 * need to start the spring context where the timezone is set on startup
 */
public class LocaleConfigTest extends AbstractMvcTestBase {

    private LocaleConfig localeConfig;
    private FileService fileService;

    private static File testFile;

    @Autowired
    public LocaleConfigTest(LocaleConfig localeConfig, FileService fileService) {
        this.localeConfig = localeConfig;
        this.fileService = fileService;
    }

    @BeforeEach
    public void initEach() {
        testFile = fileService.create(File.builder().referenceType(ReferenceType.TEMPORARY.toString()).isSystemFile(Boolean.FALSE).build());
        localeConfig.init();
    }

    @AfterEach()
    public void cleanup() {
        fileService.deletePermanently(testFile.getId());
    }

    @AfterEach
    public void cleanupEach() {
        localeConfig.init();
    }

    @Test
    public void testUTCTimeZone() {
        LocalDateTime currentServerTime = LocalDateTime.now();

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LocalDateTime utcServerTime = LocalDateTime.now();

        assertEqualsWithTolerance(currentServerTime, utcServerTime, 100);
    }

    @Test
    @Disabled("This test stopped working when CET switched to summer time... and difference become 2H instead of 1H")
    public void testUTCToGMTimeZone() {
        LocalDateTime currentServerTime = LocalDateTime.now();

        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
        LocalDateTime cetServerTime = LocalDateTime.now();

        assertEqualsWithTolerance(currentServerTime, cetServerTime.minusHours(1), 100);
    }

    @Test
    public void testLoggerTimeZone() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        assertTrue(lc.getProperty("LOG_DATEFORMAT_PATTERN").contains("UTC"), "The Logger should be configured to UTC Time Zone");
    }

    @Test
    public void testCreateEntity() {
        LocalDateTime currentServerTime = LocalDateTime.now();
        assertEqualsWithTolerance(currentServerTime, testFile.getCreatedDate(), 5000);
    }

    private void assertEqualsWithTolerance(LocalDateTime left, LocalDateTime right, int toleranceInMillis) {
        long leftMilli = left.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long rightMilli = right.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(Math.abs(leftMilli - rightMilli) <= toleranceInMillis);
    }

}
