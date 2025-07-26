package com.aldhafara.lightPollutionService.utils;

import com.aldhafara.lightPollutionService.exception.ResourceNotFoundException;
import com.aldhafara.lightPollutionService.exception.TiffFileReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Component
public class TiffFileStreamProvider implements FileStreamProvider {

    private static final Logger log = LoggerFactory.getLogger(TiffFileStreamProvider.class);

    @Override
    public InputStream getFileInputStream(String tiffUrl) throws ResourceNotFoundException, TiffFileReadException {
        try {
            InputStream inputStream;
            if (tiffUrl.startsWith("classpath:")) {
                String path = tiffUrl.substring("classpath:".length());
                inputStream = TiffFileStreamProvider.class.getResourceAsStream(path);
                if (inputStream == null) {
                    log.error("Resource not found: {}", path);
                    throw new ResourceNotFoundException(path);
                }
            } else if (tiffUrl.startsWith("file://")) {
                inputStream = new FileInputStream(new File(URI.create(tiffUrl)));
            } else {
                inputStream = new URL(tiffUrl).openStream();
            }
            return inputStream;
        } catch (MalformedURLException e) {
            log.error("Malformed URL: {}", tiffUrl, e);
            throw new TiffFileReadException("Malformed URL: " + tiffUrl, e);
        } catch (IOException e) {
            log.error("IO error opening stream for: {}", tiffUrl, e);
            throw new TiffFileReadException("IO error opening stream for: " + tiffUrl, e);
        }
    }
}
