package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.exception.TiffFileReadException;
import java.awt.image.BufferedImage;

public interface RasterImageProvider {
    BufferedImage getImage(String key);
    void getOrLoadReference(String key) throws TiffFileReadException;
}
