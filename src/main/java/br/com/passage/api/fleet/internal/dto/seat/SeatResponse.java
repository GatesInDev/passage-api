package br.com.passage.api.fleet.internal.dto.seat;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Seat details.")
public record SeatResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        boolean isActive,
        boolean isDeleted,
        UUID busId,
        String seatNumber,
        Integer row,
        Integer column,
        SeatPosition position,
        SeatQuality quality,
        boolean isAccessible
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(), seat.getCreatedAt(), seat.getUpdatedAt(),
                seat.isActive(), seat.isDeleted(), seat.getBus().getId(),
                seat.getSeatNumber(), seat.getRow(), seat.getColumn(),
                seat.getPosition(), seat.getQuality(), seat.isAccessible()
        );
    }
}
