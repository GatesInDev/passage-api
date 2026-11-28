package br.com.passage.api.route.internal.dto;

import java.math.BigDecimal;

import br.com.passage.api.route.internal.domain.enums.RouteScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Representa os dados de entrada para a atualização de uma rota.
 *
 * <p>O código e a empresa não são alterados.</p>
 */
@Schema(description = "Dados para atualizar uma rota; o código e a empresa não mudam")
public record UpdateRouteRequest
(
        @Schema(
                description = "Nome da linha",
                example = "Florianópolis - São Paulo via BR-101",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "O nome da rota é obrigatório.")
        @Size(min = 3, max = 128, message = "O nome da rota deve ter entre 3 e 128 caracteres.")
        String name,

        @Schema(
                description = "Cidade de origem",
                example = "Florianópolis",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "A cidade de origem da rota é obrigatória.")
        @Size(min = 3, max = 128, message = "A cidade de origem da rota deve ter entre 3 e 128 caracteres.")
        String originCity,

        @Schema(
                description = "UF de origem, com 2 letras maiúsculas",
                example = "SC",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 2
        )
        @NotBlank(message = "O estado de origem da rota é obrigatório.")
        @Pattern(regexp = "[A-Z]{2}", message = "O estado de origem da rota deve ter exatamente 2 caracteres.")
        String originState,

        @Schema(
                description = "Cidade de destino",
                example = "São Paulo",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 128
        )
        @NotBlank(message = "A cidade de destino da rota é obrigatória.")
        @Size(min = 3, max = 128, message = "A cidade de destino da rota deve ter entre 3 e 128 caracteres.")
        String destinationCity,

        @Schema(
                description = "UF de destino, com 2 letras maiúsculas",
                example = "SP",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2,
                maxLength = 2
        )
        @NotBlank(message = "O estado de destino da rota é obrigatório.")
        @Pattern(regexp = "[A-Z]{2}", message = "O estado de destino da rota deve ter exatamente 2 caracteres.")
        String destinationState,

        @Schema(
                description = "Distância total, em quilômetros",
                example = "705.40",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A distância da rota é obrigatória.")
        @Positive(message = "A distância da rota deve ser um valor positivo.")
        BigDecimal distanceKm,

        @Schema(
                description = "Duração estimada do trajeto, em minutos",
                example = "720",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A duração da rota é obrigatória.")
        @Positive(message = "A duração da rota deve ser um valor positivo.")
        Integer estimatedDurationMinutes,

        @Schema(
                description = "Abrangência da linha: INTRASTATE e METROPOLITAN exigem a mesma UF, INTERSTATE exige UFs diferentes",
                example = "INTERSTATE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O escopo da rota é obrigatório.")
        RouteScope serviceScope,

        @Schema(
                description = "Órgão regulador da linha",
                example = "ANTT",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64
        )
        @NotBlank(message = "O Órgão regulador da linha é obrigatório.")
        @Size(max = 64, message = "O Órgão regulador da linha deve ter no máximo 64 caracteres.")
        String regulatoryAgency,

        @Schema(
                description = "Se a rota está ativa; rotas inativas não recebem novas viagens",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O status de ativação da rota é obrigatório.")
        Boolean isActive
)
{}
