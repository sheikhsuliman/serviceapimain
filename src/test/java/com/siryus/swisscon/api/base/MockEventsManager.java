package com.siryus.swisscon.api.base;

import com.siryus.commons.rabbitmq.EventsManager;
import com.siryus.commons.rabbitmq.RabbitMessage;
import com.siryus.commons.utils.AbstractStackConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MockEventsManager<T extends RabbitMessage> extends EventsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockEventsManager.class);

    private RabbitMessage currentEvent;

    public MockEventsManager(AbstractStackConfiguration configuration) {
        super(configuration);
    }

    @Override
    public <T extends RabbitMessage> void emmit(String exchange, T event) {
        LOGGER.info(exchange);
        LOGGER.info(event.toString());
        this.currentEvent = event;
    }

    @Override
    public <T extends RabbitMessage> void emmit(String exchange, String routingKey, T event) {
        LOGGER.info(exchange);
        LOGGER.info(event.toString());
        currentEvent = event;
    }

    @Override
    public <T extends RabbitMessage> void listen(String queueName, Consumer<T> onMessage, Class<T> eventClass) {
        // do nothing
    }

    public RabbitMessage getCurrentEvent() {
        return currentEvent;
    }
}
