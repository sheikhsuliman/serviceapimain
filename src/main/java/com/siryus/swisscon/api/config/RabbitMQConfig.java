package com.siryus.swisscon.api.config;

import com.siryus.commons.rabbitmq.EventsManager;
import com.siryus.swisscon.soa.ApiConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    @ConditionalOnMissingBean(EventsManager.class)
    public EventsManager getEventsManager() {
        return new EventsManager(ApiConfiguration.configuration());
    }
}
