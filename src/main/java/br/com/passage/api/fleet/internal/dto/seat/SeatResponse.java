package br.com.passage.api.fleet.internal.dto.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de um assento")
public record SeatResponse(
        @Schema(description = "Identificador público (UUID) do assento", example = "c943e4df-618e-4194-8be6-8a43b4d83e17")
        UUID id,
        @Schema(description = "Data e hora de criação (UTC)", example = "2026-08-13T19:20:15Z")
        Instant createdAt,
        @Schema(description = "Data e hora da última alteração (UTC)", example = "2026-08-13T19:25:00Z")
        Instant updatedAt,
        @Schema(description = "Se o assento está ativo", example = "true")
        boolean isActive,
        @Schema(description = "Se o assento foi excluído logicamente", example = "false")
        boolean isDeleted,
        @Schema(description = "Ônibus do assento", example = "c943e4df-618e-4194-8be6-8a43b4d83e17")
        UUID busId,
        @Schema(description = "Número exibido ao passageiro", example = "01")
        String seatNumber,
        @Schema(description = "Fileira, a partir de 1", example = "1")
        Integer row,
        @Schema(description = "Coluna dentro da fileira, a partir de 1", example = "1")
        Integer column,
        @Schema(description = "Posição na fileira", example = "WINDOW")
        SeatPosition position,
        @Schema(description = "Categoria do assento", example = "STANDARD")
        SeatQuality quality,
        @Schema(description = "Se é reservado à acessibilidade", example = "false")
        boolean isAccessible
) {
    /** Converte a entidade na resposta da API. */
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(), seat.getCreatedAt(), seat.getUpdatedAt(),
                seat.isActive(), seat.isDeleted(), seat.getBus().getId(),
                seat.getSeatNumber(), seat.getRow(), seat.getColumn(),
                seat.getPosition(), seat.getQuality(), seat.isAccessible()
        );
    }
}
