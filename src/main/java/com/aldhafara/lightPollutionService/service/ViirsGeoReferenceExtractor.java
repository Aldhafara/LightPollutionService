package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.model.ViirsGeoReference;
import com.aldhafara.lightPollutionService.utils.FileStreamProvider;
import com.aldhafara.lightPollutionService.utils.TiffFileStreamProvider;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants.EXIF_TAG_MODEL_PIXEL_SCALE_TAG;
import static org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants.EXIF_TAG_MODEL_TIEPOINT_TAG;

@Service
public class ViirsGeoReferenceExtractor implements GeoReferenceProvider {

    private static final Logger log = LoggerFactory.getLogger(ViirsGeoReferenceExtractor.class);

    private final Map<String, ViirsGeoReference> geoRefCache = new ConcurrentHashMap<>();

    private final String viirsAverageDataPath;
    private final String viirsMaskDataPath;
    private final FileStreamProvider fileStreamProvider;

    public ViirsGeoReferenceExtractor(@Value("${viirs.average.url}") String viirsAverageDataPath,
                                      @Value("${viirs.mask.url}") String viirsMaskDataPath, FileStreamProvider fileStreamProvider) {
        this.viirsAverageDataPath = viirsAverageDataPath;
        this.viirsMaskDataPath = viirsMaskDataPath;
        this.fileStreamProvider = fileStreamProvider;
    }

    @Override
    public void getOrLoadReference(String key) {
        geoRefCache.computeIfAbsent(key, k -> {
            try {
                InputStream inputStream;
                if (key.contains("average")) {
                    inputStream = fileStreamProvider.getFileInputStream(viirsAverageDataPath);
                } else if (key.contains("mask")) {
                    inputStream = fileStreamProvider.getFileInputStream(viirsMaskDataPath);
                } else {
                    log.error("Error while reading TIFF file, unknown key='{}'", key);
                    return null;
                }
                TiffImageMetadata metadata = getTiffMetadata(inputStream);
                return extractGeoReference(metadata);

            } catch (IOException e) {
                log.error("Error while reading TIFF file", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ViirsGeoReference getReference(String key) {
        return geoRefCache.get(key);
    }

    private TiffImageMetadata getTiffMetadata(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[8192];
        int n;
        while ((n = is.read(temp)) != -1) {
            buffer.write(temp, 0, n);
        }
        byte[] tiffData = buffer.toByteArray();

        return (TiffImageMetadata) Imaging.getMetadata(tiffData);
    }

    public ViirsGeoReference extractGeoReference(TiffImageMetadata metadata) {
        double originX = 0.0;
        double originY = 0.0;
        double pixelScaleX = 0.0;
        double pixelScaleY = 0.0;
        int width = 0;
        int height = 0;

        try {
            TiffField widthField = metadata.findField(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH);
            if (widthField != null) {
                width = widthField.getIntValue();
            }
            TiffField heightField = metadata.findField(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH);
            if (heightField != null) {
                height = heightField.getIntValue();
            }
        } catch (Exception e) {
            log.error("Error while reading TIFF file metadata", e);
            throw new IllegalArgumentException("Error reading ImageWidth/ImageLength", e);
        }

        try {
            TiffField pixelScaleField = metadata.findField(EXIF_TAG_MODEL_PIXEL_SCALE_TAG);
            if (pixelScaleField != null) {
                double[] scales = (double[]) pixelScaleField.getValue();
                pixelScaleX = scales[0];
                pixelScaleY = scales[1];
            } else {
                log.error("Error while reading TIFF file metadata. Missing ModelPixelScaleTag (33550)");
                throw new IllegalArgumentException("Missing ModelPixelScaleTag (33550)");
            }
        } catch (Exception e) {
            log.error("Error while reading TIFF file metadata", e);
            throw new IllegalArgumentException("Error reading ModelPixelScaleTag", e);
        }

        try {
            TiffField tiepointField = metadata.findField(EXIF_TAG_MODEL_TIEPOINT_TAG);
            if (tiepointField != null) {
                double[] tiepoints = (double[]) tiepointField.getValue();
                if (tiepoints.length >= 5) {
                    originX = tiepoints[3];
                    originY = tiepoints[4];
                } else {
                    log.error("Error while reading TIFF file metadata. Incorrect ModelTiepointTag length");
                    throw new IllegalArgumentException("Incorrect ModelTiepointTag length");
                }
            } else {
                log.error("Error while reading TIFF file metadata. Missing ModelTiepointTag (33922)");
                throw new IllegalArgumentException("Missing ModelTiepointTag (33922)");
            }
        } catch (Exception e) {
            log.error("Error while reading TIFF file metadata", e);
            throw new IllegalArgumentException("Error reading ModelTiepointTag", e);
        }

        return new ViirsGeoReference(originX, originY, pixelScaleX, pixelScaleY, width, height);
    }
}
