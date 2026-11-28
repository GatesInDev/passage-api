package br.com.passage.api.trip.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação detalhada dos dados de resposta de uma viagem")
public record TripResponse(

        @Schema(
                description = "Identificador único (UUID) público da viagem",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação do registro (UTC)",
                example = "2026-08-13T19:20:15Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última atualização do registro (UTC)",
                example = "2026-08-13T19:25:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Indica se a viagem está ativa para operação no sistema",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Indica se o registro passou por exclusão lógica (soft delete)",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Código identificador da viagem",
                example = "FLN-SP-20261110-0800"
        )
        String code,

        @Schema(
                description = "UUID da rota percorrida pela viagem",
                example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0"
        )
        UUID routeId,

        @Schema(
                description = "UUID da empresa responsável pela viagem (a mesma da rota)",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid,

        @Schema(
                description = "UUID do ônibus alocado para a viagem",
                example = "3f1c2b7a-8d4e-4a6b-9c1d-2e5f7a8b9c0d"
        )
        UUID busId,

        @Schema(
                description = "UUID do motorista responsável pela viagem",
                example = "7c9e6679-7425-40de-944b-e07fc1f90ae7"
        )
        UUID driverId,

        @Schema(
                description = "Data e hora de partida da viagem (UTC)",
                example = "2026-11-10T11:00:00Z"
        )
        Instant departureTime,

        @Schema(
                description = "Data e hora de chegada prevista da viagem (UTC)",
                example = "2026-11-10T23:00:00Z"
        )
        Instant arrivalTime,

        @Schema(
                description = "Situação atual da viagem",
                example = "SCHEDULED"
        )
        TripStatus status,

        @Schema(
                description = "Preço base da passagem da viagem",
                example = "189.90"
        )
        BigDecimal price
)
{
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
