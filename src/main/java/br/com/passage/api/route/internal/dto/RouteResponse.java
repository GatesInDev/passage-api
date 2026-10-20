package br.com.passage.api.route.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.domain.enums.RouteScope;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação detalhada dos dados de resposta de uma rota")
public record RouteResponse(

        @Schema(
                description = "Identificador único (UUID) público da rota",
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
                description = "Indica se a rota está ativa para operação no sistema",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Indica se o registro passou por exclusão lógica (soft delete)",
                example = "false"
        )
        boolean isDeleted,

        @Schema(
                description = "Código ou prefixo da linha registrado no órgão regulador",
                example = "1234-56"
        )
        String code,

        @Schema(
                description = "UUID da empresa dona da rota",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid,

        @Schema(
                description = "Nome descritivo da linha/rota",
                example = "Florianópolis - São Paulo via BR-101"
        )
        String name,

        @Schema(
                description = "Cidade de origem da rota",
                example = "Florianópolis"
        )
        String originCity,

        @Schema(
                description = "Unidade Federativa (UF) de origem",
                example = "SC"
        )
        String originState,

        @Schema(
                description = "Cidade de destino da rota",
                example = "São Paulo"
        )
        String destinationCity,

        @Schema(
                description = "Unidade Federativa (UF) de destino",
                example = "SP"
        )
        String destinationState,

        @Schema(
                description = "Distância total da rota em quilômetros",
                example = "705.40"
        )
        BigDecimal distanceKm,

        @Schema(
                description = "Tempo estimado de viagem, em minutos",
                example = "720"
        )
        Integer estimatedDurationMinutes,

        @Schema(
                description = "Abrangência do serviço da linha, que determina o órgão regulador",
                example = "INTERSTATE"
        )
        RouteScope serviceScope,

        @Schema(
                description = "Órgão regulador ou concedente da linha (ex.: ANTT, DETER-SC)",
                example = "ANTT"
        )
        String regulatoryAgency

) {
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
