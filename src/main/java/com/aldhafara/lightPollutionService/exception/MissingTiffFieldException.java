package com.aldhafara.lightPollutionService.exception;

public class MissingTiffFieldException extends TiffMetadataExtractionException {
    public MissingTiffFieldException(String fieldName) {
        super("Missing TIFF field: " + fieldName);
    }
}
