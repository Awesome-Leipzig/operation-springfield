package com.springfield.plant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plant.security")
public record PlantSecurityProperties(String apiKey) {

    public boolean apiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
