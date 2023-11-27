package com.siryus.swisscon.api.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateConverter {

    public static LocalDateTime toUtcLocalDateTime(ZonedDateTime zonedDateTime) {
        if(zonedDateTime != null) {
            return LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC);
        }
        return null;
    }

    public static ZonedDateTime toUtcZonedDateTime(LocalDateTime localDateTime) {
        if(localDateTime != null) {
            return localDateTime.atZone(ZoneId.of("UTC"));
        }
        return null;
    }
}
