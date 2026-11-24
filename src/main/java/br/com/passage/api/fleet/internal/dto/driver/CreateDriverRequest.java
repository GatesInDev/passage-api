package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Create a driver.")
public record CreateDriverRequest(
        @NotBlank @Size(max = 100)
        String name,
        @NotBlank @Size(max = 20)
        String licenseNumber,
        @Size(max = 5)
        String licenseCategory,
        @Schema(description = "Required for administrators; managers always use their authenticated company.")
        UUID companyUuid
) {}
