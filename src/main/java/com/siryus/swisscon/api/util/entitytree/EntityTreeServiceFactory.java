package com.siryus.swisscon.api.util.entitytree;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityTreeServiceFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public EntityTreeService createService(List<String> containerTypes) {
        return (EntityTreeService) applicationContext.getBean(EntityTreeService.ENTITY_TREE_SERVICE, containerTypes);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
