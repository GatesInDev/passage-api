package br.com.passage.api.fleet.internal.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update a bus.")
public record UpdateBusRequest(
        @NotBlank @Size(max = 64)
        String code,
        @NotBlank @Size(max = 128)
        String model,
        @NotBlank @Size(max = 128)
        String manufacturer,
        @NotBlank @Size(max = 8)
        String licensePlate,
        @NotNull
        Boolean isActive
) {}
