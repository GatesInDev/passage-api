package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Dados para cadastrar um motorista")
public record CreateDriverRequest(
        @Schema(description = "Nome completo", example = "João Pereira", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "Número da CNH, único no sistema", example = "02650306461", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 20)
        String licenseNumber,
        @Schema(description = "Categoria da CNH", example = "D")
        @Size(max = 5)
        String licenseCategory,
        @Schema(description = "Empresa dona: obrigatória para ADMIN e ignorada para FLEET_MANAGER, que usa a própria empresa")
        UUID companyUuid
) {}
