package com.takima.backskeleton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.inventory")
public record InventoryProperties(
    int expiringSoonDays
) {}