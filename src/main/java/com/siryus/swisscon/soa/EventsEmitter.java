package com.siryus.swisscon.soa;

import com.siryus.commons.rabbitmq.EventsManager;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.soa.cache.dto.CacheUpdateEvent;
import com.siryus.swisscon.soa.cache.event.RabbitMqConnectionFactory;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EventsEmitter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventsEmitter.class);

    private static final String DOMAIN_EVENTS_EXCHANGE = "domain-events-exchange";
    private final EventsManager eventsManager;

    @Autowired
    public EventsEmitter(EventsManager eventsManager) {
        var configuration = ApiConfiguration.configuration();

        LOGGER.info("RABBITMQ_HOST = {}", configuration.get(ApiConfiguration.Var.RABBITMQ_HOST));

        this.eventsManager = eventsManager;
    }

    public void emitCacheUpdate(ReferenceType referenceType, Integer referenceId) {
        eventsManager.emmit(RabbitMqConnectionFactory.CACHE_UPDATES_EXCHANGE, new CacheUpdateEvent(referenceType.name(),referenceId));
    }

    public void emitNotification(NotificationEvent notificationEvent) {
        eventsManager.emmit(DOMAIN_EVENTS_EXCHANGE, notificationEvent);
    }
}
