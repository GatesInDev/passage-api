package br.com.passage.api.route.internal.dto;

import java.math.BigDecimal;

import br.com.passage.api.route.internal.domain.enums.RouteScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload com os dados necessários para a atualização de uma rota existente")
public record UpdateRouteRequest
(
        @Schema(
                description = "Nome descritivo da linha/rota",
                example = "Florianópolis - São Paulo via BR-101",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "O nome da rota é obrigatório.")
        @Size(min = 3, max = 128, message = "O nome da rota deve ter entre 3 e 128 caracteres.")
        String name,

        @Schema(
                description = "Cidade de origem da rota",
                example = "Florianópolis",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "A cidade de origem da rota é obrigatória.")
        @Size(min = 3, max = 128, message = "A cidade de origem da rota deve ter entre 3 e 128 caracteres.")
        String originCity,

        @Schema(
                description = "Unidade Federativa (UF) de origem, com 2 letras maiúsculas",
                example = "SC",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 2
        )
        @NotBlank(message = "O estado de origem da rota é obrigatório.")
        @Pattern(regexp = "[A-Z]{2}", message = "O estado de origem da rota deve ter exatamente 2 caracteres.")
        String originState,

        @Schema(
                description = "Cidade de destino da rota",
                example = "São Paulo",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "A cidade de destino da rota é obrigatória.")
        @Size(min = 3, max = 128, message = "A cidade de destino da rota deve ter entre 3 e 128 caracteres.")
        String destinationCity,

        @Schema(
                description = "Unidade Federativa (UF) de destino, com 2 letras maiúsculas",
                example = "SP",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 2
        )
        @NotBlank(message = "O estado de destino da rota é obrigatório.")
        @Pattern(regexp = "[A-Z]{2}", message = "O estado de destino da rota deve ter exatamente 2 caracteres.")
        String destinationState,

        @Schema(
                description = "Distância total da rota em quilômetros",
                example = "705.40",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A distância da rota é obrigatória.")
        @Positive(message = "A distância da rota deve ser um valor positivo.")
        BigDecimal distanceKm,

        @Schema(
                description = "Tempo estimado de viagem, em minutos",
                example = "720",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A duração da rota é obrigatória.")
        @Positive(message = "A duração da rota deve ser um valor positivo.")
        Integer estimatedDurationMinutes,

        @Schema(
                description = "Abrangência do serviço da linha, que determina o órgão regulador",
                example = "INTERSTATE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O escopo da rota é obrigatório.")
        RouteScope serviceScope,

        @Schema(
                description = "Órgão regulador ou concedente da linha (ex.: ANTT, DETER-SC)",
                example = "ANTT",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64
        )
        @NotBlank(message = "O Órgão regulador da linha é obrigatório.")
        @Size(max = 64, message = "O Órgão regulador da linha deve ter no máximo 64 caracteres.")
        String regulatoryAgency,

        @Schema(
                description = "Define se a rota está ativa para operação",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O status de ativação da rota é obrigatório.")
        Boolean isActive
)
{}
