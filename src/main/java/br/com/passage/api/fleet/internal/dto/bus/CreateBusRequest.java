package br.com.passage.api.fleet.internal.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Create a bus.")
public record CreateBusRequest(
        @NotBlank @Size(max = 64)
        String code,
        @NotBlank @Size(max = 128)
        String model,
        @NotBlank @Size(max = 128)
        String manufacturer,
        @NotBlank @Size(max = 8)
        String licensePlate,
        @Schema(description = "Required for administrators; managers always use their authenticated company.")
        UUID companyUuid
) {}
