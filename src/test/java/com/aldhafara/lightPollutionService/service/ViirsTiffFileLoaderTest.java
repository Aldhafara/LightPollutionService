package com.aldhafara.lightPollutionService.service;

import org.apache.commons.imaging.Imaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class ViirsTiffFileLoaderTest {

    private ViirsTiffFileLoader loader;

    @BeforeEach
    void setUp() throws URISyntaxException {
        URL averageResourceUrl = getClass().getClassLoader().getResource("tiff/mock_average.tiff");
        URL maskResourceUrl = getClass().getClassLoader().getResource("tiff/mock_mask.tiff");

        loader = new ViirsTiffFileLoader(
                averageResourceUrl.toURI().toString(),
                maskResourceUrl.toURI().toString()
        );
    }

    @Test
    void testGetOrLoadReference_CachesImage_ForAverageKey() throws Exception {
        try (MockedStatic<Imaging> imaging = mockStatic(Imaging.class)) {
            BufferedImage mockedImage = mock(BufferedImage.class);
            imaging.when(() -> Imaging.getBufferedImage(any(InputStream.class))).thenReturn(mockedImage);

            loader.getOrLoadReference("2023/average");
            BufferedImage result = loader.getImage("2023/average");

            assertNotNull(result);
            imaging.verify(() -> Imaging.getBufferedImage(any(InputStream.class)), times(1));
        }
    }

    @Test
    void testGetOrLoadReference_CachesImage_ForMaskKey() throws Exception {
        try (MockedStatic<Imaging> imaging = mockStatic(Imaging.class)) {
            BufferedImage mockedImage = mock(BufferedImage.class);
            imaging.when(() -> Imaging.getBufferedImage(any(InputStream.class))).thenReturn(mockedImage);

            loader.getOrLoadReference("2023/mask");
            BufferedImage result = loader.getImage("2023/mask");

            assertNotNull(result);
            imaging.verify(() -> Imaging.getBufferedImage(any(InputStream.class)), times(1));
        }
    }

    @Test
    void testGetOrLoadReference_IgnoresUnknownKey() {
        loader.getOrLoadReference("2023/unknown");
        assertNull(loader.getImage("2023/unknown"));
    }

    @Test
    void testGetOrLoadReference_HandlesIOException() {
        ViirsTiffFileLoader failingLoader = new ViirsTiffFileLoader(
                "file:///nonexistent/average.tiff", "file:///nonexistent/mask.tiff"
        );
        assertThrows(RuntimeException.class, () -> {
            failingLoader.getOrLoadReference("2023/average");
        });
    }
}
