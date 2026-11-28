package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Dados para cadastrar um motorista")
public record CreateDriverRequest(
        @Schema(description = "Nome completo", example = "João Pereira", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O nome do motorista é obrigatório.")
        @Size(max = 100, message = "O nome do motorista deve ter no máximo 100 caracteres.")
        String name,
        @Schema(description = "Número da CNH, único no sistema", example = "02650306461", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O número da CNH é obrigatório.")
        @Size(max = 20, message = "O número da CNH deve ter no máximo 20 caracteres.")
        String licenseNumber,
        @Schema(description = "Categoria da CNH", example = "D")
        @Size(max = 5, message = "A categoria da CNH deve ter no máximo 5 caracteres.")
        String licenseCategory,
        @Schema(description = "Empresa dona: obrigatória para ADMIN e ignorada para FLEET_MANAGER, que usa a própria empresa")
        UUID companyUuid
) {}
