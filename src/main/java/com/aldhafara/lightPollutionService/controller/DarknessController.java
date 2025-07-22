package com.aldhafara.lightPollutionService.controller;

import com.aldhafara.lightPollutionService.model.DarknessResponse;
import com.aldhafara.lightPollutionService.service.ViirsTiffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/darkness")
public class DarknessController {

    private final ViirsTiffService tiffService;

    public DarknessController(ViirsTiffService tiffService) {
        this.tiffService = tiffService;
    }

    @Operation(
            summary = "Get relative brightness for given coordinates",
            description = "Returns a value [0-255] indicating relative night sky brightness for the provided latitude and longitude. "
                    + "Higher value means more light pollution.",
            parameters = {
                    @Parameter(
                            name = "latitude",
                            description = "Latitude in decimal degrees",
                            required = true,
                            example = "52.2298"
                    ),
                    @Parameter(
                            name = "longitude",
                            description = "Longitude in decimal degrees",
                            required = true,
                            example = "21.0117"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully found pixel value",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DarknessResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameter or coordinate out of raster bounds",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content
                    )
            }
    )
    @GetMapping
    public ResponseEntity<DarknessResponse> getDarkness(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        Double relBrightness = tiffService.getValueForLocation(latitude, longitude);
        DarknessResponse response = new DarknessResponse(latitude, longitude, relBrightness);
        return ResponseEntity.ok(response);
    }
}
