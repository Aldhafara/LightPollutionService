package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.exception.CoordinatesOutOfRasterBoundsException;
import com.aldhafara.lightPollutionService.exception.TiffFileReadException;
import com.aldhafara.lightPollutionService.model.ViirsGeoReference;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class ViirsTiffService {

    private static final Logger log = LoggerFactory.getLogger(ViirsTiffService.class);

    private final RasterImageProvider imageProvider;
    private final GeoReferenceProvider referenceProvider;

    public ViirsTiffService(RasterImageProvider imageProvider, GeoReferenceProvider referenceProvider) {
        this.imageProvider = imageProvider;
        this.referenceProvider = referenceProvider;
    }

    @PostConstruct
    public void init() {
        getOrLoadReference("2023/average");
    }

    public Double getValueForLocation(double lat, double lon, BufferedImage tiffFile, ViirsGeoReference geoReference) throws CoordinatesOutOfRasterBoundsException {
        int x = (int) ((lon - geoReference.originX()) / geoReference.pixelScaleX());
        int y = (int) ((geoReference.originY() - lat) / geoReference.pixelScaleY());

        if (x < 0 || x >= geoReference.width() || y < 0 || y >= geoReference.height()) {
            log.warn("Coordinates lat:'{}', lon:'{}' outside the TIFF raster range", lat, lon);
            throw new CoordinatesOutOfRasterBoundsException(
                    String.format("Coordinates lat:%.8f, lon:%.8f are outside the TIFF raster range", lat, lon)
            );
        }

        int pixel = tiffFile.getRGB(x, y);

        double alpha = (pixel >> 24) & 0xff;
        double red = (pixel >> 16) & 0xff;
        double green = (pixel >> 8) & 0xff;
        double blue = (pixel) & 0xff;

        return red;
    }

    public Double getValueForLocation(double lat, double lon) throws CoordinatesOutOfRasterBoundsException {
        return getValueForLocation(lat, lon, imageProvider.getImage("2023/average"), referenceProvider.getReference("2023/average"));
    }

    public void getOrLoadReference(String key) throws TiffFileReadException {
        imageProvider.getOrLoadReference(key);
        referenceProvider.getOrLoadReference(key);
    }
}
