package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update a driver.")
public record UpdateDriverRequest(
        @NotBlank @Size(max = 100)
        String name,
        @NotBlank @Size(max = 20)
        String licenseNumber,
        @Size(max = 5)
        String licenseCategory,
        @NotNull
        Boolean isActive
) {}
