package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.model.ViirsGeoReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViirsTiffServiceTest {
    private RasterImageProvider mockImageProvider;
    private GeoReferenceProvider mockReferenceProvider;
    private ViirsTiffService service;

    @BeforeEach
    void setUp() {
        mockImageProvider = mock(RasterImageProvider.class);
        mockReferenceProvider = mock(GeoReferenceProvider.class);
        service = new ViirsTiffService(mockImageProvider, mockReferenceProvider);
    }

    @Test
    void getValueForLocation_returnsRedValue_whenCoordinatesAreInBounds() {
        ViirsGeoReference ref = mock(ViirsGeoReference.class);
        when(ref.originX()).thenReturn(10.0);
        when(ref.originY()).thenReturn(20.0);
        when(ref.pixelScaleX()).thenReturn(1.0);
        when(ref.pixelScaleY()).thenReturn(1.0);
        when(ref.width()).thenReturn(50);
        when(ref.height()).thenReturn(50);

        BufferedImage image = mock(BufferedImage.class);
        int expectedRed = 123;
        int mockPixel = (255 << 24) | (expectedRed << 16);
        when(image.getRGB(15, 10)).thenReturn(mockPixel);

        double lat = 10;
        double lon = 25;

        double red = service.getValueForLocation(lat, lon, image, ref);
        assertEquals(expectedRed, red);
    }

    @Test
    void getValueForLocation_throwsException_whenCoordinatesOutOfBounds() {
        ViirsGeoReference ref = mock(ViirsGeoReference.class);
        when(ref.originX()).thenReturn(0.0);
        when(ref.originY()).thenReturn(0.0);
        when(ref.pixelScaleX()).thenReturn(1.0);
        when(ref.pixelScaleY()).thenReturn(1.0);
        when(ref.width()).thenReturn(5);
        when(ref.height()).thenReturn(5);

        BufferedImage image = mock(BufferedImage.class);

        double lat = 7.0;
        double lon = 6.0;

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getValueForLocation(lat, lon, image, ref)
        );
        assertTrue(ex.getMessage().contains("outside the TIFF raster range"));
    }

    @Test
    void getValueForLocation_noArgs_delegatesToProviders() {
        ViirsGeoReference ref = mock(ViirsGeoReference.class);
        when(ref.originX()).thenReturn(0.0);
        when(ref.originY()).thenReturn(0.0);
        when(ref.pixelScaleX()).thenReturn(1.0);
        when(ref.pixelScaleY()).thenReturn(1.0);
        when(ref.width()).thenReturn(1);
        when(ref.height()).thenReturn(1);

        BufferedImage image = mock(BufferedImage.class);
        when(image.getRGB(anyInt(), anyInt())).thenReturn(42 << 16);

        when(mockImageProvider.getImage("2023/average")).thenReturn(image);
        when(mockReferenceProvider.getReference("2023/average")).thenReturn(ref);

        double red = service.getValueForLocation(0.0, 0.0);

        assertEquals(42, red);
        verify(mockImageProvider, times(1)).getImage("2023/average");
        verify(mockReferenceProvider, times(1)).getReference("2023/average");
    }

    @Test
    void getOrLoadReference_delegatesToProviders() {
        service.getOrLoadReference("2023/average");
        verify(mockImageProvider, times(1)).getOrLoadReference("2023/average");
        verify(mockReferenceProvider, times(1)).getOrLoadReference("2023/average");
    }
}
