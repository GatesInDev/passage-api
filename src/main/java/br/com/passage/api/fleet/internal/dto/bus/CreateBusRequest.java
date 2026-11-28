package br.com.passage.api.fleet.internal.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Dados para cadastrar um ônibus")
public record CreateBusRequest(
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
        @Schema(description = "Empresa dona: obrigatória para ADMIN e ignorada para FLEET_MANAGER, que usa a própria empresa")
        UUID companyUuid
) {}
