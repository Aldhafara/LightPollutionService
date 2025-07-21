package com.aldhafara.lightPollutionService.service;

import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ViirsTiffFileLoader implements RasterImageProvider {

    private static final Logger log = LoggerFactory.getLogger(ViirsTiffFileLoader.class);

    private final String viirsAverageDataPath;
    private final String viirsMaskDataPath;

    private final Map<String, BufferedImage> geoRefCacheStream = new ConcurrentHashMap<>();

    public ViirsTiffFileLoader(@Value("${viirs.average.url}") String viirsAverageDataPath,
                               @Value("${viirs.mask.url}") String viirsMaskDataPath) {
        this.viirsAverageDataPath = viirsAverageDataPath;
        this.viirsMaskDataPath = viirsMaskDataPath;
    }

    @Override
    public void getOrLoadReference(String key) {

        geoRefCacheStream.computeIfAbsent(key, k -> {
            try {
                InputStream inputStream;
                if (key.contains("average")) {
                    inputStream = getFileInputStream(viirsAverageDataPath);
                } else if (key.contains("mask")) {
                    inputStream = getFileInputStream(viirsMaskDataPath);
                } else {
                    return null;
                }
                return Imaging.getBufferedImage(inputStream);

            } catch (IOException e) {
                log.error("Error while reading TIFF file", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public BufferedImage getImage(String key) {
        return geoRefCacheStream.get(key);
    }

    private InputStream getFileInputStream(String tiffUrl) throws IOException {
        if (tiffUrl.startsWith("file://")) {
            return new FileInputStream(new File(URI.create(tiffUrl)));
        } else {
            return new URL(tiffUrl).openStream();
        }
    }
}
