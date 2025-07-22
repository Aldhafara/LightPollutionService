package com.aldhafara.lightPollutionService.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Component
public class TiffFileStreamProvider implements FileStreamProvider{

    @Override
    public InputStream getFileInputStream(String tiffUrl) throws IOException {
        InputStream inputStream;
        if (tiffUrl.startsWith("classpath:")) {
            String path = tiffUrl.substring("classpath:".length());
            inputStream = TiffFileStreamProvider.class.getResourceAsStream(path);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + path);
            }
        } else if (tiffUrl.startsWith("file://")) {
            inputStream = new FileInputStream(new File(URI.create(tiffUrl)));
        } else {
            inputStream = new URL(tiffUrl).openStream();
        }
        return inputStream;
    }
}
