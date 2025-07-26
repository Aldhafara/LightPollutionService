package com.aldhafara.lightPollutionService.utils;

import com.aldhafara.lightPollutionService.exception.ResourceNotFoundException;
import com.aldhafara.lightPollutionService.exception.TiffFileReadException;

import java.io.InputStream;

public interface FileStreamProvider {
    InputStream getFileInputStream(String tiffUrl) throws ResourceNotFoundException, TiffFileReadException;
}
