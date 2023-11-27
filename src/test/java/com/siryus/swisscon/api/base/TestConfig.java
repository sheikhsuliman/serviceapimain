package com.siryus.swisscon.api.base;

import com.siryus.commons.rabbitmq.EventsManager;
import com.siryus.swisscon.soa.ApiConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is to override beans (to use mock beans instead)
 */
@Configuration
@EnableScheduling
public class TestConfig {

    @Bean
    @Primary
    public EventsManager getEventsManager() {
        return new MockEventsManager(ApiConfiguration.configuration());
    }
}
