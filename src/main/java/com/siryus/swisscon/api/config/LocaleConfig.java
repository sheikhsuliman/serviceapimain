package com.siryus.swisscon.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
public class LocaleConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleConfig.class);

    /**
     * Register a formatter for parsing LocalDateTime
     */
    @Bean
    public Formatter<LocalDateTime> localDateFormatter() {
        return new Formatter<LocalDateTime>() {
            @Override
            public LocalDateTime parse(String text, Locale locale) {
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            }

            @Override
            public String print(LocalDateTime object, Locale locale) {
                return DateTimeFormatter.ISO_DATE_TIME.format(object);
            }
        };
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LOGGER.info("Spring boot application running in UTC timezone :" + LocalDateTime.now().toString());
    }
}
