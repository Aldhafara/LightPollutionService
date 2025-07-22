package com.aldhafara.lightPollutionService.utils;

import java.io.IOException;
import java.io.InputStream;

public interface FileStreamProvider {
    InputStream getFileInputStream(String tiffUrl) throws IOException;
}
