package com.siryus.swisscon.api.exceptions;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalizedResponseStatusException extends ResponseStatusException {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalizedResponseStatusException.class);

    private static final int UID_LENGTH = 5;

    public static final int AUTH_ERROR_CODE         =  1000;
    public static final int SECURITY_ERROR_CODE     =  2000;
    public static final int GENERAL_ERROR_CODE      =  3000;
    public static final int COMPANY_ERROR_CODE      =  4000;
    public static final int CONFIG_ERROR_CODE       =  5000;
    public static final int FILE_ERROR_CODE         =  6000;
    public static final int LOCATION_ERROR_CODE     =  7000;
    public static final int MEDIA_WIDGET_ERROR_CODE =  8000;
    public static final int CATALOG_ERROR_CODE      =  9000;
    public static final int PROJECT_ERROR_CODE      = 10000;
    public static final int SPECIFICATION_ERROR_CODE= 11000;
    public static final int TASKS_ERROR_CODE        = 12000;
    public static final int TASK_WORK_LOG_ERROR_CODE= 13000;
    public static final int CATALOG_CSV_ERROR_CODE =  14000;
    public static final int CUSTOM_ROLE_ERROR_CODE =  15000;
    public static final int CONTRACT_ERROR_CODE    =  16000;

    public static final int VALIDATION_ERROR_CODE   = 90000;


    /**
     * UNAUTHORIZED - To perform an operation, user has to be logged in
     * NOT_PERMITTED - To perform an operation, user has to have particular permission
     * BAD_REQUEST - Request is badly formatted. Mandatory fields are missing... or null
     * TARGET_NOT_FOUND - Request refer object which is missing or deleted
     * BUSINESS_LOGIC_ERROR - Request properly formatted... and target objects are present... but they internal state
     *   cause operation to fail
     * INTERNAL_ERROR - Something really wrong inside the BE... time to call tech support
     */
    public static final HttpStatus UNAUTHORIZED = HttpStatus.UNAUTHORIZED;
    public static final HttpStatus NOT_PERMITTED = HttpStatus.FORBIDDEN;
    public static final HttpStatus BAD_REQUEST = HttpStatus.BAD_REQUEST;
    public static final HttpStatus TARGET_NOT_FOUND = HttpStatus.NOT_FOUND;
    public static final HttpStatus BUSINESS_LOGIC_ERROR = HttpStatus.CONFLICT;
    public static final HttpStatus INTERNAL_ERROR = HttpStatus.INTERNAL_SERVER_ERROR;


    private final LocalizedReason localizedReason;
    private final String uid;

    LocalizedResponseStatusException(HttpStatus status, LocalizedReason localizedReason) {
        super(status, localizedReason.getReason());
        this.localizedReason = localizedReason;

        this.uid = generateUid();

        printCleanStackTrace();
    }

    public LocalizedReason getLocalizedReason() {
        return localizedReason;
    }

    public String getUid() {
        return uid;
    }

    public static LocalizedResponseStatusException withLocalizedReason(HttpStatus status, LocalizedReason localizedReason) {
        return new LocalizedResponseStatusException(status, localizedReason);
    }

    public static LocalizedResponseStatusException notPermitted(LocalizedReason localizedReason) {
        return withLocalizedReason(NOT_PERMITTED, localizedReason);
    }

    public static LocalizedResponseStatusException unAuthorized(LocalizedReason localizedReason) {
        return withLocalizedReason(UNAUTHORIZED, localizedReason);
    }

    public static LocalizedResponseStatusException badRequest(LocalizedReason localizedReason) {
        return withLocalizedReason(BAD_REQUEST, localizedReason);
    }

    public static LocalizedResponseStatusException notFound(LocalizedReason localizedReason) {
        return withLocalizedReason(TARGET_NOT_FOUND, localizedReason);
    }

    public static LocalizedResponseStatusException businessLogicError(LocalizedReason localizedReason) {
        return withLocalizedReason(BUSINESS_LOGIC_ERROR, localizedReason);
    }

    public static LocalizedResponseStatusException internalError(LocalizedReason localizedReason) {
        return withLocalizedReason(INTERNAL_ERROR, localizedReason);
    }

    private String generateUid() {
        return RandomStringUtils.random( UID_LENGTH, true, false ).toUpperCase();
    }

    private void printCleanStackTrace() {
        StringBuilder cleanStackTrace = new StringBuilder();
        AtomicBoolean gap = new AtomicBoolean(false);

        Arrays.stream(getStackTrace()).forEach( t -> {
            String className = t.getClassName();

            if (className.equals(LocalizedResponseStatusException.class.getName())) {
                // do nothing
            }
            else if (
                    className.startsWith("com.siryus.swisscon")
                            && (!t.getFileName().equals("<generated>"))
                            && (!t.getMethodName().startsWith("doFilter"))
            ) {
                if (gap.getAndSet(false)) {
                    cleanStackTrace.append("...\n");
                }
                cleanStackTrace.append("   ");
                cleanStackTrace.append(className );
                cleanStackTrace.append(".");
                cleanStackTrace.append(t.getMethodName());
                cleanStackTrace.append("() [");
                cleanStackTrace.append(t.getFileName());
                cleanStackTrace.append(":");
                cleanStackTrace.append(t.getLineNumber());
                cleanStackTrace.append("]");
                cleanStackTrace.append("\n");
            }
            else {
                gap.set(true);
            }
        });
        if (gap.getAndSet(false)) {
            cleanStackTrace.append("...\n");
        }

        LOGGER.error("[{}] {} : {} - {} \n{}", this.uid, this.getStatus().name(), this.getLocalizedReason().getErrorCode(), this.getReason(), cleanStackTrace);
    }
}
