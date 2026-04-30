package com.cis.api.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;


@Component
@Profile("!test")
public class SystemStateConfig {

    private final AtomicBoolean isV1Sunset = new AtomicBoolean(false);

    public boolean isV1Sunset() {
        return isV1Sunset.get();
    }

    public void setV1Sunset(boolean value) {
        isV1Sunset.set(value);
    }
}