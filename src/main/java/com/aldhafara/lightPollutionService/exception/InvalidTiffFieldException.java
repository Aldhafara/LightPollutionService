package com.aldhafara.lightPollutionService.exception;

public class InvalidTiffFieldException extends TiffMetadataExtractionException {
    public InvalidTiffFieldException(String fieldName, String message) {
        super("Invalid TIFF field " + fieldName + ": " + message);
    }
}
