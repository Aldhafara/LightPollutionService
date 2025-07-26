package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.exception.ResourceNotFoundException;
import com.aldhafara.lightPollutionService.exception.TiffFileReadException;
import com.aldhafara.lightPollutionService.utils.FileStreamProvider;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ViirsTiffFileLoader implements RasterImageProvider {

    private static final Logger log = LoggerFactory.getLogger(ViirsTiffFileLoader.class);

    private final String viirsAverageDataPath;
    private final String viirsMaskDataPath;
    private final FileStreamProvider fileStreamProvider;

    private final Map<String, BufferedImage> geoRefCacheStream = new ConcurrentHashMap<>();

    public ViirsTiffFileLoader(@Value("${viirs.average.url}") String viirsAverageDataPath,
                               @Value("${viirs.mask.url}") String viirsMaskDataPath, FileStreamProvider fileStreamProvider) {
        this.viirsAverageDataPath = viirsAverageDataPath;
        this.viirsMaskDataPath = viirsMaskDataPath;
        this.fileStreamProvider = fileStreamProvider;
    }

    @Override
    public void getOrLoadReference(String key) throws TiffFileReadException {
        try {
            geoRefCacheStream.computeIfAbsent(key, k -> {
                try {
                    InputStream inputStream;
                    if (k.contains("average")) {
                        inputStream = fileStreamProvider.getFileInputStream(viirsAverageDataPath);
                    } else if (k.contains("mask")) {
                        inputStream = fileStreamProvider.getFileInputStream(viirsMaskDataPath);
                    } else {
                        log.error("Error while reading TIFF file, unknown key='{}'", k);
                        return null;
                    }
                    return Imaging.getBufferedImage(inputStream);
                } catch (ResourceNotFoundException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ResourceNotFoundException || cause instanceof TiffFileReadException) {
                throw new TiffFileReadException("Failed to read TIFF file", cause);
            } else {
                throw e;
            }
        }
    }

    @Override
    public BufferedImage getImage(String key) {
        return geoRefCacheStream.get(key);
    }
}
