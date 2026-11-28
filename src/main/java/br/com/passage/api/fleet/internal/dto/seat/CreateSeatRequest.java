package br.com.passage.api.fleet.internal.dto.seat;

import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Representa os dados de entrada para o cadastro de um assento.
 *
 * <p>O número e a posição (fileira e coluna) devem ser únicos no ônibus.</p>
 */
@Schema(description = "Dados para cadastrar um assento; número e posição são únicos no ônibus")
public record CreateSeatRequest(
        @Schema(description = "Número exibido ao passageiro", example = "01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O número do assento é obrigatório.")
        @Size(max = 8, message = "O número do assento deve ter no máximo 8 caracteres.")
        String seatNumber,
        @Schema(description = "Fileira, a partir de 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A fileira do assento é obrigatória.")
        @Positive(message = "A fileira do assento deve ser maior que zero.")
        Integer row,
        @Schema(description = "Coluna dentro da fileira, a partir de 1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A coluna do assento é obrigatória.")
        @Positive(message = "A coluna do assento deve ser maior que zero.")
        Integer column,
        @Schema(description = "Posição na fileira", example = "WINDOW", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A posição do assento é obrigatória.")
        SeatPosition position,
        @Schema(description = "Categoria do assento", example = "STANDARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A categoria do assento é obrigatória.")
        SeatQuality quality,
        @Schema(description = "Se é reservado à acessibilidade", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A indicação de acessibilidade é obrigatória.")
        Boolean isAccessible
) {}
