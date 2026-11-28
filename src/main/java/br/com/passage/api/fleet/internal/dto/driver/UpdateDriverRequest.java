package br.com.passage.api.fleet.internal.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar um motorista; a empresa não muda")
public record UpdateDriverRequest(
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
        @Schema(description = "Se o motorista está ativo; motoristas inativos não podem ser alocados em viagens", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O status de ativação do motorista é obrigatório.")
        Boolean isActive
) {}
