package com.aldhafara.lightPollutionService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusServiceTest {

    private StatusService service;

    @BeforeEach
    void setUp() {
        service = new StatusService(2, 60);
    }

    @Test
    void getUptime() throws InterruptedException {
        Thread.sleep(1);
        long firstUptime = service.getUptime();
        assertTrue(firstUptime > 0, "Uptime should be greater that 0");
        Thread.sleep(5);
        long secondUptime = service.getUptime();
        assertTrue(secondUptime > firstUptime, "Uptime should increase over time");
    }

    @Test
    void getTimestamp() {
        String timestamp = service.getTimestamp();
        assertFalse(timestamp.isEmpty(), "Timestamp should not be empty");
        assertTrue(timestamp.startsWith("20"), "Timestamp should be in ISO 8601 format");
        assertTrue(timestamp.contains("T"), "Timestamp should be in ISO 8601 format");
        assertTrue(timestamp.contains("Z"), "Timestamp should be in ISO 8601 format");
    }
}