package com.aldhafara.lightPollutionService.model;

public record ApiErrorResponse(String timestamp, int status, String error, String message) {}
