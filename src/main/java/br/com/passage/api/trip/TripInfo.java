package br.com.passage.api.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;

public record TripInfo(
        UUID id,
        UUID companyUuid,
        UUID busId,
        Instant departureTime,
        BigDecimal price,
        TripStatus status
) {}