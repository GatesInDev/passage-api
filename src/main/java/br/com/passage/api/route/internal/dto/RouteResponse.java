package br.com.passage.api.route.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.domain.enums.RouteScope;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa os dados de uma rota retornados pela API.
 */
@Schema(description = "Dados de uma rota")
public record RouteResponse(

        @Schema(
                description = "Identificador público (UUID) da rota",
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
                description = "Se a rota está ativa; rotas inativas não recebem novas viagens",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Se a rota foi excluída logicamente",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Prefixo da linha no órgão regulador",
                example = "1234-56"
        )
        String code,

        @Schema(
                description = "Empresa operadora",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid,

        @Schema(
                description = "Nome da linha",
                example = "Florianópolis - São Paulo via BR-101"
        )
        String name,

        @Schema(
                description = "Cidade de origem",
                example = "Florianópolis"
        )
        String originCity,

        @Schema(
                description = "UF de origem",
                example = "SC"
        )
        String originState,

        @Schema(
                description = "Cidade de destino",
                example = "São Paulo"
        )
        String destinationCity,

        @Schema(
                description = "UF de destino",
                example = "SP"
        )
        String destinationState,

        @Schema(
                description = "Distância total, em quilômetros",
                example = "705.40"
        )
        BigDecimal distanceKm,

        @Schema(
                description = "Duração estimada do trajeto, em minutos",
                example = "720"
        )
        Integer estimatedDurationMinutes,

        @Schema(
                description = "Abrangência da linha",
                example = "INTERSTATE"
        )
        RouteScope serviceScope,

        @Schema(
                description = "Órgão regulador da linha",
                example = "ANTT"
        )
        String regulatoryAgency

) {
        /**
         * Converte a rota persistida em sua representação de resposta.
         *
         * @param route rota de origem
         * @return representação de resposta correspondente
         */
        public static RouteResponse from(Route route){
                return new RouteResponse(
            route.getId(),
            route.getCreatedAt(),
            route.getUpdatedAt(),
            route.isActive(),
            route.isDeleted(),
            route.getCode(),
            route.getCompanyUuid(),
            route.getName(),
            route.getOriginCity(),
            route.getOriginState(),
            route.getDestinationCity(),
            route.getDestinationState(),
            route.getDistanceKm(),
            route.getEstimatedDurationMinutes(),
            route.getServiceScope(),
            route.getRegulatoryAgency() 
        );
        }
}
