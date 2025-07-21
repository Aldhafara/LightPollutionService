package com.aldhafara.lightPollutionService.model;

public record ViirsGeoReference(
        double originX,
        double originY,
        double pixelScaleX,
        double pixelScaleY,
        int width,
        int height
) {
    public int[] latLonToRasterXY(double lat, double lon) {
        int x = (int) ((lon - originX) / pixelScaleX);
        int y = (int) ((originY - lat) / pixelScaleY);
        return new int[] { x, y };
    }
}