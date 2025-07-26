package com.aldhafara.lightPollutionService.exception;

public class TiffMetadataExtractionException extends RuntimeException {
    public TiffMetadataExtractionException(String message) {
        super(message);
    }

    public TiffMetadataExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}

