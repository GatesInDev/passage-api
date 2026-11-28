package br.com.passage.api.fleet.internal.dto.bus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Representa os dados de entrada para o cadastro de um ônibus.
 *
 * <p>A empresa é obrigatória para o administrador. Para o gestor de frota, é
 * sempre a própria empresa.</p>
 */
@Schema(description = "Dados para cadastrar um ônibus")
public record CreateBusRequest(
        @Schema(description = "Código interno na operação da empresa", example = "ONB-1020", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O código do ônibus é obrigatório.")
        @Size(max = 64, message = "O código do ônibus deve ter no máximo 64 caracteres.")
        String code,
        @Schema(description = "Modelo", example = "Paradiso 1800 DD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O modelo do ônibus é obrigatório.")
        @Size(max = 128, message = "O modelo do ônibus deve ter no máximo 128 caracteres.")
        String model,
        @Schema(description = "Fabricante", example = "Marcopolo", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O fabricante do ônibus é obrigatório.")
        @Size(max = 128, message = "O fabricante do ônibus deve ter no máximo 128 caracteres.")
        String manufacturer,
        @Schema(description = "Placa, única no sistema", example = "ABC1D23", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A placa do ônibus é obrigatória.")
        @Size(max = 8, message = "A placa do ônibus deve ter no máximo 8 caracteres.")
        String licensePlate,
        @Schema(description = "Empresa dona: obrigatória para ADMIN e ignorada para FLEET_MANAGER, que usa a própria empresa")
        UUID companyUuid
) {}
