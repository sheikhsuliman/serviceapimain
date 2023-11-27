package com.siryus.swisscon.api.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ServletCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);

        mappings.add("eot", "application/vnd.ms-fontobject");
        mappings.add("eot?fvnb2j", "application/vnd.ms-fontobject");
        mappings.add("eot?fvnb2j#swisscon-icons", "application/vnd.ms-fontobject");

        mappings.add("otf", "font/opentype");
        mappings.add("otf?fvnb2j", "font/opentype");

        mappings.add("ttf", "application/x-font-ttf");
        mappings.add("ttf?fvnb2j", "application/x-font-ttf");

        mappings.add("woff", "application/x-font-woff");
        mappings.add("woff?fvnb2j", "application/x-font-woff");

        mappings.add("svg", "image/svg+xml");

        mappings.add("woff2", "application/x-font-woff2");
        mappings.add("woff2?fvnb2j", "application/x-font-woff2");
        
        factory.setMimeMappings(mappings);
    }
}
