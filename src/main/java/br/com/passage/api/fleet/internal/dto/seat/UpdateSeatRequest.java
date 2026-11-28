package br.com.passage.api.fleet.internal.dto.seat;

import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizar um assento; número e posição são únicos no ônibus")
public record UpdateSeatRequest(
        @Schema(description = "Número exibido ao passageiro", example = "01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 8) String seatNumber,
        @Schema(description = "Fileira, a partir de 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer row,
        @Schema(description = "Coluna dentro da fileira, a partir de 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer column,
        @Schema(description = "Posição na fileira", example = "WINDOW", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull SeatPosition position,
        @Schema(description = "Categoria do assento", example = "STANDARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull SeatQuality quality,
        @Schema(description = "Se é reservado à acessibilidade", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean isAccessible,
        @Schema(description = "Se o assento está ativo; só assentos ativos contam na capacidade", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean isActive
) {}
