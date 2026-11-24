package br.com.passage.api.fleet.internal.dto.seat;

import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Update a seat on a bus.")
public record UpdateSeatRequest(
        @NotBlank @Size(max = 8) String seatNumber,
        @NotNull @Positive Integer row,
        @NotNull @Positive Integer column,
        @NotNull SeatPosition position,
        @NotNull SeatQuality quality,
        @NotNull Boolean isAccessible,
        @NotNull Boolean isActive
) {}
