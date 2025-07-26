package com.aldhafara.lightPollutionService.controller;

import com.aldhafara.lightPollutionService.ratelimit.RateLimitAspect;
import com.aldhafara.lightPollutionService.service.ViirsTiffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(DarknessControllerTest.RateLimitTestConfig.class)
@WebMvcTest(DarknessController.class)
class DarknessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViirsTiffService tiffService;

    @Autowired
    private RateLimitAspect rateLimitAspect;

    @BeforeEach
    void setUp() {
        rateLimitAspect.resetLimiters();
    }

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

    @Test
    void shouldReturn429AfterThirdRequestDueToRateLimiting() throws Exception {
        when(tiffService.getValueForLocation(52.2298, 21.0117)).thenReturn(128.0);

        mockMvc.perform(get("/darkness")
                        .param("latitude", "52.2298")
                        .param("longitude", "21.0117")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/darkness")
                        .param("latitude", "52.2298")
                        .param("longitude", "21.0117")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/darkness")
                        .param("latitude", "52.2298")
                        .param("longitude", "21.0117")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }

    @TestConfiguration
    static class RateLimitTestConfig {
        @Bean
        public RateLimitAspect rateLimitAspect(Environment env) {
            return new RateLimitAspect(env);
        }
    }

    @Test
    void shouldReturnBadRequest_whenLatitudeIsTooLow() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "-91")
                        .param("longitude", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request parameters"))
                .andExpect(jsonPath("$.message").value("getDarkness.latitude: must be greater than or equal to -90"));
    }

    @Test
    void shouldReturnBadRequest_whenLatitudeIsTooHigh() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "91")
                        .param("longitude", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request parameters"))
                .andExpect(jsonPath("$.message").value("getDarkness.latitude: must be less than or equal to 90"));
    }

    @Test
    void shouldReturnBadRequest_whenLongitudeIsTooLow() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "20")
                        .param("longitude", "-181")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request parameters"))
                .andExpect(jsonPath("$.message").value("getDarkness.longitude: must be greater than or equal to -180"));
    }

    @Test
    void shouldReturnBadRequest_whenLongitudeIsTooHigh() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "20")
                        .param("longitude", "181")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request parameters"))
                .andExpect(jsonPath("$.message").value("getDarkness.longitude: must be less than or equal to 180"));
    }

    @Test
    void shouldReturnBadRequest_whenLongitudeIsBadType() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "20")
                        .param("longitude", "cow")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid parameter: longitude"));
    }

    @Test
    void shouldReturnBadRequest_whenLatitudeIsBadType() throws Exception {
        mockMvc.perform(get("/darkness")
                        .param("latitude", "cow")
                        .param("longitude", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid parameter: latitude"));
    }
}
