package br.com.passage.api.trip.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para atualizar uma viagem; o código e a rota não mudam")
public record UpdateTripRequest
(
        @Schema(
                description = "Ônibus alocado: da mesma empresa da rota, ativo e sem outra viagem no período",
                example = "3f1c2b7a-8d4e-4a6b-9c1d-2e5f7a8b9c0d",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O ônibus da viagem é obrigatório.")
        UUID busId,

        @Schema(
                description = "Motorista alocado: da mesma empresa da rota, ativo e sem outra viagem no período",
                example = "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O motorista da viagem é obrigatório.")
        UUID driverId,

        @Schema(
                description = "Partida prevista (UTC)",
                example = "2026-11-10T11:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O horário de partida da viagem é obrigatório.")
        Instant departureTime,

        @Schema(
                description = "Chegada prevista (UTC), posterior à partida",
                example = "2026-11-10T23:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O horário de chegada da viagem é obrigatório.")
        Instant arrivalTime,

        @Schema(
                description = "Preço base da passagem",
                example = "189.90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O preço da viagem é obrigatório.")
        @Positive(message = "O preço da viagem deve ser um valor positivo.")
        BigDecimal price,

        @Schema(
                description = "Nova situação. Ao cancelar, os demais campos são ignorados",
                example = "SCHEDULED",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A situação da viagem é obrigatória.")
        TripStatus status
)
{}
