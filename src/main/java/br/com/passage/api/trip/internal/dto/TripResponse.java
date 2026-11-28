package br.com.passage.api.trip.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de uma viagem")
public record TripResponse(

        @Schema(
                description = "Identificador público (UUID) da viagem",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação (UTC)",
                example = "2026-08-13T19:20:15Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última alteração (UTC)",
                example = "2026-08-13T19:25:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Se a viagem está ativa",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Se a viagem foi excluída logicamente",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Código da viagem",
                example = "FLN-SP-20261110-0800"
        )
        String code,

        @Schema(
                description = "Rota percorrida",
                example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0"
        )
        UUID routeId,

        @Schema(
                description = "Empresa operadora, a mesma da rota",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid,

        @Schema(
                description = "Ônibus alocado",
                example = "3f1c2b7a-8d4e-4a6b-9c1d-2e5f7a8b9c0d"
        )
        UUID busId,

        @Schema(
                description = "Motorista alocado",
                example = "7c9e6679-7425-40de-944b-e07fc1f90ae7"
        )
        UUID driverId,

        @Schema(
                description = "Partida prevista (UTC)",
                example = "2026-11-10T11:00:00Z"
        )
        Instant departureTime,

        @Schema(
                description = "Chegada prevista (UTC)",
                example = "2026-11-10T23:00:00Z"
        )
        Instant arrivalTime,

        @Schema(
                description = "Situação atual",
                example = "SCHEDULED"
        )
        TripStatus status,

        @Schema(
                description = "Preço base da passagem",
                example = "189.90"
        )
        BigDecimal price
)
{
    /** Converte a entidade na resposta da API. */
    public static TripResponse from(Trip trip){
        return new TripResponse(
            trip.getId(),
            trip.getCreatedAt(),
            trip.getUpdatedAt(),
            trip.isActive(),
            trip.isDeleted(),
            trip.getCode(),
            trip.getRouteId(),
            trip.getCompanyUuid(),
            trip.getBusId(),
            trip.getDriverId(),
            trip.getDepartureTime(),
            trip.getArrivalTime(),
            trip.getStatus(),
            trip.getPrice()
        );
    }

}
