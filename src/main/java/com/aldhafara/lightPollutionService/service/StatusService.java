package com.aldhafara.lightPollutionService.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class StatusService {

    @Getter
    private final int ratelimitRequests;

    @Getter
    private final int ratelimitDurationSeconds;

    private final Instant startTime = Instant.now();

    public StatusService(@Value("${ratelimit.requests}") int ratelimitRequests,
                         @Value("${ratelimit.durationSeconds}") int ratelimitDurationSeconds) {
        this.ratelimitRequests = ratelimitRequests;
        this.ratelimitDurationSeconds = ratelimitDurationSeconds;
    }

    public long getUptimeMillis() {
        return Duration.between(startTime, Instant.now()).toMillis();
    }

    public String getTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now().atZone(ZoneOffset.UTC));
    }
}
