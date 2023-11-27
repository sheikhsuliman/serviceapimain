package com.siryus.swisscon.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.function.Supplier;

@Configuration
public class ClockConfig {
    public static Clock clock = Clock.systemDefaultZone();

    public static void setClock(Clock clock) {
        ClockConfig.clock = clock;
    }

    @Bean
    Supplier<Clock> getClock() {
        return () -> clock;
    }
}
