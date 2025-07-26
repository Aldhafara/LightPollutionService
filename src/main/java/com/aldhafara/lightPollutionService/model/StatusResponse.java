package com.aldhafara.lightPollutionService.model;

public record StatusResponse(String status, long uptime, String uptimePretty, String timestamp, int ratelimitRequests, int ratelimitDurationSeconds) {
}
