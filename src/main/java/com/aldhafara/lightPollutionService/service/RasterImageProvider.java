package com.aldhafara.lightPollutionService.service;

import java.awt.image.BufferedImage;

public interface RasterImageProvider {
    BufferedImage getImage(String key);
    void getOrLoadReference(String key);
}
