package com.aldhafara.lightPollutionService.service;

import com.aldhafara.lightPollutionService.model.ViirsGeoReference;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViirsGeoReferenceExtractorTest {

    private ViirsGeoReferenceExtractor extractor;

    @BeforeEach
    void setUp() throws URISyntaxException {
        URL averageResourceUrl = getClass().getClassLoader().getResource("tiff/mock_average.tiff");
        URL maskResourceUrl = getClass().getClassLoader().getResource("tiff/mock_mask.tiff");

        extractor = new ViirsGeoReferenceExtractor(
                averageResourceUrl.toURI().toString(),
                maskResourceUrl.toURI().toString()
        );
    }

    @Test
    void testGetOrLoadReferenceCachesReferenceWhenAverage() throws Exception {
        TiffImageMetadata fakeMetadata = mock(TiffImageMetadata.class);
        ViirsGeoReference fakeReference = new ViirsGeoReference(1.0, 2.0, 3.0, 4.0, 100, 200);

        try (MockedStatic<org.apache.commons.imaging.Imaging> imagingStatic = Mockito.mockStatic(org.apache.commons.imaging.Imaging.class)) {
            imagingStatic.when(() -> org.apache.commons.imaging.Imaging.getMetadata(any(byte[].class))).thenReturn(fakeMetadata);

            ViirsGeoReferenceExtractor spyExtractor = Mockito.spy(extractor);
            doReturn(fakeReference).when(spyExtractor).extractGeoReference(fakeMetadata);
            doReturn(new ByteArrayInputStream(new byte[]{1, 2, 3})).when(spyExtractor).getFileInputStream(anyString());

            spyExtractor.getOrLoadReference("2023/average");
            ViirsGeoReference cachedReference = spyExtractor.getReference("2023/average");

            assertNotNull(cachedReference);
            assertEquals(fakeReference, cachedReference);
        }
    }

    @Test
    void testGetOrLoadReferenceReturnsNullAndLogsOnUnknownKey() {
        extractor.getOrLoadReference("2023/unknown");
        ViirsGeoReference result = extractor.getReference("2023/unknown");
        assertNull(result);
    }

    @Test
    void testGetOrLoadReferenceThrowsRuntimeExceptionOnIOException() throws Exception {
        ViirsGeoReferenceExtractor spyExtractor = Mockito.spy(extractor);
        doThrow(new java.io.IOException("File error")).when(spyExtractor).getFileInputStream(anyString());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            spyExtractor.getOrLoadReference("2023/average");
        });
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void testExtractGeoReferenceWithProperMetadata() throws ImagingException {
        TiffImageMetadata metadata = Mockito.mock(TiffImageMetadata.class);
        TiffField widthField = Mockito.mock(TiffField.class);
        TiffField heightField = Mockito.mock(TiffField.class);
        TiffField pixelScaleField = Mockito.mock(TiffField.class);
        TiffField tiepointField = Mockito.mock(TiffField.class);

        when(metadata.findField(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg.equals(org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.TIFF_TAG_IMAGE_WIDTH)) {
                return widthField;
            } else if (arg.equals(org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.TIFF_TAG_IMAGE_LENGTH)) {
                return heightField;
            } else if (arg.toString().contains("PixelScale")) {
                return pixelScaleField;
            } else if (arg.toString().contains("Tiepoint")) {
                return tiepointField;
            }
            return null;
        });

        when(widthField.getIntValue()).thenReturn(5631);
        when(heightField.getIntValue()).thenReturn(3449);
        when(pixelScaleField.getValue()).thenReturn(new double[]{0.00417, 0.00417});
        when(tiepointField.getValue()).thenReturn(new double[]{0, 0, 0, 6.57, 58.72});

        ViirsGeoReference geoReference = extractor.extractGeoReference(metadata);

        assertEquals(6.57, geoReference.originX(), 0.0001);
        assertEquals(58.72, geoReference.originY(), 0.0001);
        assertEquals(0.00417, geoReference.pixelScaleX(), 0.0001);
        assertEquals(0.00417, geoReference.pixelScaleY(), 0.0001);
        assertEquals(5631, geoReference.width());
        assertEquals(3449, geoReference.height());
    }
}
