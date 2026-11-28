package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar um motorista; a empresa não muda")
public record UpdateDriverRequest(
        @Schema(description = "Nome completo", example = "João Pereira", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "Número da CNH, único no sistema", example = "02650306461", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 20)
        String licenseNumber,
        @Schema(description = "Categoria da CNH", example = "D")
        @Size(max = 5)
        String licenseCategory,
        @Schema(description = "Se o motorista está ativo; motoristas inativos não podem ser alocados em viagens", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean isActive
) {}
