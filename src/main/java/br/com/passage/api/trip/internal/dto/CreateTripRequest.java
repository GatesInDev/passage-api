package br.com.passage.api.trip.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para agendar uma viagem")
public record CreateTripRequest
(
        @Schema(
                description = "Código da viagem, único e não alterável depois",
                example = "FLN-SP-20261110-0800",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64
        )
        @NotBlank(message = "O código da viagem é obrigatório.")
        @Size(max = 64, message = "O código da viagem deve ter no máximo 64 caracteres.")
        String code,

        @Schema(
                description = "Rota percorrida, ativa; a viagem herda a empresa dela",
                example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A rota da viagem é obrigatória.")
        UUID routeId,

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
                description = "Partida prevista (UTC), no futuro",
                example = "2026-11-10T11:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O horário de partida da viagem é obrigatório.")
        @Future(message = "O horário de partida da viagem deve ser uma data futura.")
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
        BigDecimal price
)
{}
