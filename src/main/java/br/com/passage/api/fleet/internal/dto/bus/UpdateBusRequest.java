package br.com.passage.api.fleet.internal.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar um ônibus; a empresa não muda")
public record UpdateBusRequest(
        @Schema(description = "Código interno na operação da empresa", example = "ONB-1020", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 64)
        String code,
        @Schema(description = "Modelo", example = "Paradiso 1800 DD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 128)
        String model,
        @Schema(description = "Fabricante", example = "Marcopolo", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 128)
        String manufacturer,
        @Schema(description = "Placa, única no sistema", example = "ABC1D23", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 8)
        String licensePlate,
        @Schema(description = "Se o ônibus está ativo; ônibus inativos não podem ser alocados em viagens", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean isActive
) {}
