package com.zycus.ziprun.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfiguration {

    @Value("${routing.strategy:AI}")
    private String activeStrategy;

    public String getActiveStrategy() {
        return activeStrategy.toUpperCase();
    }
}