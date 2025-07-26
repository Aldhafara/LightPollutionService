package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.exception.TiffFileReadException;
import com.aldhafara.lightPollutionService.model.ViirsGeoReference;

public interface GeoReferenceProvider {
    ViirsGeoReference getReference(String key);
    void getOrLoadReference(String key) throws TiffFileReadException;
}
