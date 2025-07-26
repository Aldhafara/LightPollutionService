package com.aldhafara.lightPollutionService.controller;

import com.aldhafara.lightPollutionService.model.StatusResponse;
import com.aldhafara.lightPollutionService.service.StatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class StatusController {
    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @Operation(
            summary = "Service health-check",
            description = "Returns the server's uptime status, uptime and timestamp.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service is working",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StatusResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/status")
    public StatusResponse getStatus() {

        long uptime = statusService.getUptime();
        return new StatusResponse(
                "UP",
                uptime,
                getUptimePretty(uptime),
                statusService.getTimestamp(),
                statusService.getRatelimitRequests(),
                statusService.getRatelimitDurationSeconds()
        );
    }

    private String getUptimePretty(long uptime) {
        int seconds = (int) (uptime / 1000);
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return hours + "h " + minutes + "m " + secs + "s";
    }
}
