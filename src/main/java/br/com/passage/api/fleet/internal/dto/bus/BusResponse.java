package br.com.passage.api.fleet.internal.dto.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de um ônibus")
public record BusResponse(
        @Schema(description = "Identificador público (UUID) do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17")
        UUID id,
        @Schema(description = "Data e hora de criação (UTC)", example = "2026-08-13T19:20:15Z")
        Instant createdAt,
        @Schema(description = "Data e hora da última alteração (UTC)", example = "2026-08-13T19:25:00Z")
        Instant updatedAt,
        @Schema(description = "Se o ônibus está ativo", example = "true")
        boolean isActive,
        @Schema(description = "Se o ônibus foi excluído logicamente", example = "false")
        boolean isDeleted,
        @Schema(description = "Empresa dona do ônibus", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID companyUuid,
        @Schema(description = "Código interno na operação da empresa", example = "ONB-1020")
        String code,
        @Schema(description = "Modelo", example = "Paradiso 1800 DD")
        String model,
        @Schema(description = "Fabricante", example = "Marcopolo")
        String manufacturer,
        @Schema(description = "Placa", example = "ABC1D23")
        String licensePlate,
        @Schema(description = "Quantidade de assentos ativos", example = "46")
        int capacity
) {
    /** Converte a entidade na resposta da API. */
    public static BusResponse from(Bus bus) {
        return new BusResponse(
                bus.getId(),
                bus.getCreatedAt(),
                bus.getUpdatedAt(),
                bus.isActive(),
                bus.isDeleted(),
                bus.getCompany().getId(),
                bus.getCode(),
                bus.getModel(),
                bus.getManufacturer(),
                bus.getLicensePlate(),
                bus.getCapacity()
        );
    }
}
