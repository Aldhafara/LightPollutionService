package com.aldhafara.lightPollutionService.controller;

import com.aldhafara.lightPollutionService.service.ViirsTiffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DarknessController.class)
class DarknessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViirsTiffService tiffService;

    @Test
    void shouldReturnBrightnessValue_whenValidParameters() throws Exception {
        when(tiffService.getValueForLocation(52.2298, 21.0117)).thenReturn(128.0);

        mockMvc.perform(get("/darkness")
                        .param("latitude", "52.2298")
                        .param("longitude", "21.0117")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"latitude\":52.2298,\"longitude\":21.0117,\"relativeBrightness\":128.0}"));
    }

    @Test
    void shouldReturnBadRequest_whenMissingParameters() throws Exception {
        mockMvc.perform(get("/darkness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerError_whenServiceThrows() throws Exception {
        when(tiffService.getValueForLocation(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/darkness")
                        .param("latitude", "52.2298")
                        .param("longitude", "21.0117")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
