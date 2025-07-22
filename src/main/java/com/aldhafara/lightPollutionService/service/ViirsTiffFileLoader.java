package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.utils.FileStreamProvider;
import com.aldhafara.lightPollutionService.utils.TiffFileStreamProvider;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void getOrLoadReference(String key) {

        geoRefCacheStream.computeIfAbsent(key, k -> {
            try {
                InputStream inputStream;
                if (key.contains("average")) {
                    inputStream = fileStreamProvider.getFileInputStream(viirsAverageDataPath);
                } else if (key.contains("mask")) {
                    inputStream = fileStreamProvider.getFileInputStream(viirsMaskDataPath);
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
}
